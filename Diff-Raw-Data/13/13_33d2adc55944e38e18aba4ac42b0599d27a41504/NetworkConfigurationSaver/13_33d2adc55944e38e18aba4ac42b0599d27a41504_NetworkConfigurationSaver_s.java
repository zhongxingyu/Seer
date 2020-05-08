 /* AWE - Amanzi Wireless Explorer
  * http://awe.amanzi.org
  * (C) 2008-2009, AmanziTel AB
  *
  * This library is provided under the terms of the Eclipse Public License
  * as described at http://www.eclipse.org/legal/epl-v10.html. Any use,
  * reproduction or distribution of the library constitutes recipient's
  * acceptance of this agreement.
  *
  * This library is distributed WITHOUT ANY WARRANTY; without even the
  * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  */
 
 package org.amanzi.awe.afp.ericsson.saver;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.amanzi.awe.afp.ericsson.parser.NetworkConfigurationFileTypes;
 import org.amanzi.awe.afp.ericsson.parser.NetworkConfigurationTransferData;
 import org.amanzi.neo.loader.core.saver.AbstractHeaderSaver;
 import org.amanzi.neo.loader.core.saver.IStructuredSaver;
 import org.amanzi.neo.loader.core.saver.MetaData;
 import org.amanzi.neo.services.DatasetService;
 import org.amanzi.neo.services.DatasetService.NodeResult;
 import org.amanzi.neo.services.GisProperties;
 import org.amanzi.neo.services.INeoConstants;
 import org.amanzi.neo.services.NeoServiceFactory;
 import org.amanzi.neo.services.NetworkService;
 import org.amanzi.neo.services.enums.DatasetRelationshipTypes;
 import org.amanzi.neo.services.enums.GeoNeoRelationshipTypes;
 import org.amanzi.neo.services.enums.NodeTypes;
 import org.amanzi.neo.services.network.FrequencyPlanModel;
 import org.amanzi.neo.services.network.NetworkModel;
 import org.amanzi.neo.services.node2node.NodeToNodeRelationModel;
 import org.amanzi.neo.services.utils.Utils;
 import org.apache.commons.lang.StringUtils;
 import org.neo4j.graphdb.Direction;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.Path;
 import org.neo4j.graphdb.Relationship;
 import org.neo4j.graphdb.traversal.Evaluation;
 import org.neo4j.graphdb.traversal.Evaluator;
 import org.neo4j.graphdb.traversal.Traverser;
 import org.neo4j.kernel.Traversal;
 import org.neo4j.kernel.Uniqueness;
 
 /**
  * <p>
  * Saver for Network configuration data
  * </p>
  * 
  * @author TsAr
  * @since 1.0.0
  */
 public class NetworkConfigurationSaver extends AbstractHeaderSaver<NetworkConfigurationTransferData>
         implements
             IStructuredSaver<NetworkConfigurationTransferData> {
     private final MetaData metadata = new MetaData("network", MetaData.SUB_TYPE, "radio");
     private NetworkConfigurationFileTypes type;
     private NetworkService networkService;
     // private Node neighbourRoot;
     private String neighName;
     private Node bsc;
     private Map<String, String> tgProperty = new HashMap<String, String>();
     private Pattern tgPat = Pattern.compile("(^.*)(-)(\\d+$)", Pattern.CASE_INSENSITIVE);
     private Pattern trxPat = Pattern.compile("(^.*)(-)(\\d+)(-)(\\d+$)", Pattern.CASE_INSENSITIVE);
     private NetworkModel networkModel;
     private NodeToNodeRelationModel neighbourModel;
     private DatasetService ds;
     // private String planName;
     private FrequencyPlanModel freqPlan;
     private int sectorsNotFound;
     private int neighboursNotFound;
     private Boolean haveBsm;
 
     private final static String ORIGINAL = "original";
 
     @Override
     public void init(NetworkConfigurationTransferData element) {
         super.init(element);
         ds = NeoServiceFactory.getInstance().getDatasetService();
         networkService = NeoServiceFactory.getInstance().getNetworkService();
         networkModel = new NetworkModel(rootNode);
         neighName = networkModel.makeUniqueListName("Neighbours");
         neighbourModel = networkModel.getNeighbours(neighName);
         // planName = null;
         freqPlan = null;
         sectorsNotFound = 0;
         neighboursNotFound = 0;
         haveBsm = Boolean.valueOf(element.get("haveBSM"));
         startMainTx(2000);
     }
 
     @Override
     public void save(NetworkConfigurationTransferData element) {
         if (type == NetworkConfigurationFileTypes.CNA) {
             saveCNALine(element);
         } else {
             saveBSMLine(element);
         }
     }
 
     /**
      * @param element
      */
     private void saveCNALine(NetworkConfigurationTransferData element) {
         String bscName = getStringValue("BSC", element);
         // TODO add vendor as property of BSC if necessary - but not from file - maybe from parser
 
         if (StringUtils.isEmpty(bscName)) {
             info(String.format("Line N%s not parsed - field '%s' is empty", element.getLine(), "BSC"));
             return;
         }
         NodeResult bscNode = networkService.getBscNode(rootNode, bscName, rootNode);
         if (bscNode.isCreated()) {
             statistic.updateTypeCount(rootname, NodeTypes.BSC.getId(), 1);
         }
         String sectorName = getStringValue("CELL", element);
         if (StringUtils.isEmpty(sectorName)) {
             info(String.format("Line N%s - field '%s' is empty", element.getLine(), "cell"));
             return;
         }
         String siteName = getStringValue("SITE", element);
         if (siteName == null) {
             siteName = sectorName.substring(0, sectorName.length() - 1);
         }
         if (StringUtils.isEmpty(siteName)) {
             info(String.format("Line N%s - field '%s' is empty", element.getLine(), "SITE"));
             return;
         }
         NodeResult site = networkService.getSite(rootNode, siteName, bscNode);
         if (site.isCreated()) {
             statistic.updateTypeCount(rootname, NodeTypes.SITE.getId(), 1);
         }
         Integer ci = getNumberValue(Integer.class, "ci", element);
         Integer lac = getNumberValue(Integer.class, "lac", element);
 
         Node sector = networkService.findSector(rootNode, ci, lac, sectorName, true);
         if (sector == null) {
             error(String.format("Sector '%s' not saved. Reason: sector do not found in physical data.", sectorName));
             return;
         } else {
             networkService.moveSectorToCorrectSite(site, sector);
         }
         if (ci != null && !sector.hasProperty(INeoConstants.PROPERTY_SECTOR_CI)) {
             setProperty(rootname, NodeTypes.SECTOR.getId(), sector, INeoConstants.PROPERTY_SECTOR_CI, ci);
             getIndexService().index(sector,
                     Utils.getLuceneIndexKeyByProperty(rootNode, INeoConstants.PROPERTY_SECTOR_CI, NodeTypes.SECTOR), ci);
         }
         if (lac != null && !sector.hasProperty(INeoConstants.PROPERTY_SECTOR_LAC)) {
             setProperty(rootname, NodeTypes.SECTOR.getId(), sector, INeoConstants.PROPERTY_SECTOR_LAC, lac);
             getIndexService().index(sector,
                     Utils.getLuceneIndexKeyByProperty(rootNode, INeoConstants.PROPERTY_SECTOR_LAC, NodeTypes.SECTOR), lac);
         }
         updateProperty(rootname, NodeTypes.SECTOR.getId(), sector, "vendor", "Ericsson");
         String bcc = getStringValue("bcc", element);
         boolean notEmptyBcc = StringUtils.isNotEmpty(bcc);
         String ncc = getStringValue("ncc", element);
         boolean notEmptyNcc = StringUtils.isNotEmpty(ncc);
         if (notEmptyBcc) {
             updateProperty(rootname, NodeTypes.SECTOR.getId(), sector, "bcc", bcc);
         }
         if (notEmptyNcc) {
             updateProperty(rootname, NodeTypes.SECTOR.getId(), sector, "ncc", ncc);
         }
         int bccInt = notEmptyBcc ? Integer.valueOf(bcc) : 0;
         int nccint = notEmptyNcc ? Integer.valueOf(ncc) : 0;
         networkService.indexProperty(rootNode, sector, "BSIC", nccint * 10 + bccInt);
         Integer bcchno = getNumberValue(Integer.class, "bcchno", element);
         if (bcchno != null) {
             networkService.indexProperty(rootNode, sector, "bcch", bcchno);
         }
         updateProperty(rootname, NodeTypes.SECTOR.getId(), sector, "bcch", bcchno);
         for (int i = 0; i < 16; i++) {
             if (getStringValue("ch_group_" + i, element) != null) {
                 storeChannelInfo(sector, i, element);
             }
         }
         for (int i = 0; i <= 63; i++) {
             String neighbour = getStringValue("n_cell_" + i, element);
             if (StringUtils.isNotEmpty(neighbour)) {
                 Node neighbourSector = networkService.findSector(rootNode, null, null, neighbour, true);
                 if (neighbourSector == null) {
                     if (neighboursNotFound++ < 10) {
                         info(String.format("Line %s: Neighbour sector with name %s not found", element.getLine(), neighbour));
                     }
                     continue;
                 }
                 Relationship rel = neighbourModel.getRelation(sector, neighbourSector);
                 updateProperty(neighName, NodeTypes.NODE_NODE_RELATIONS.getId(), rel, "co", new Double(0.5d));
                 updateProperty(neighName, NodeTypes.NODE_NODE_RELATIONS.getId(), rel, "adj", new Double(0.05));
             }
         }
         statistic.setTypeCount(neighName, NodeTypes.NODE_NODE_RELATIONS.getId(), neighbourModel.getRelationCount());
         statistic.setTypeCount(neighName, NodeTypes.PROXY.getId(), neighbourModel.getRelationCount());
 
         updateTx(5, 5);// not real values. for real values methods getXXX should return NodeResult
                        // values
 
     }
 
     /**
      * Store channel info.
      * 
      * @param sector the sector
      * @param i the i
      * @param element the element
      */
     private void storeChannelInfo(Node sector, int i, NetworkConfigurationTransferData element) {
         Node channalGr = networkService.getChannelGroupNode(sector, i);
         String tg = getStringValue(i, "chgr_tg", element);
         if (StringUtils.isNotEmpty(tg)) {
             channalGr.setProperty("tg", tg);
         }
         String sctype = getStringValue(i, "sctype", element);
         if (StringUtils.isNotEmpty(sctype)) {
             channalGr.setProperty("sctype", sctype);
         }
         String band = getStringValue(i, "band", element);
         if (StringUtils.isNotEmpty(band)) {
             channalGr.setProperty("band", band);
         }
         String hop = getStringValue(i, "hop", element);
         if (StringUtils.isNotEmpty(hop)) {
             channalGr.setProperty("hop", hop);
         }
         String hsn = getStringValue(i, "hsn", element);
         if (StringUtils.isNotEmpty(hsn)) {
             if (hsn != null && !"default".equalsIgnoreCase(hsn)) {
                 try {
                     Integer val = Integer.parseInt(hsn);
                     channalGr.setProperty("hsn", val);
                 } catch (NumberFormatException e) {
                     // do nothing
                 }
             }
         }
         int[] dccno = new int[64];
         int j = 0;
         for (int ind = 0; ind < 64; ind++) {
            String valStr = getStringValue(i, "dchno_" + ind, element);
             if (valStr != null) {
                 try {
                     int valInt = Integer.parseInt(valStr);
                     dccno[j++] = valInt;
                 } catch (NumberFormatException e) {
                     // do nothing
                 }
 
             }
         }
         int[] dccnoArr = Arrays.copyOf(dccno, j);
         channalGr.setProperty("dchno", dccnoArr);
         for (int mId = 0; mId < 16; mId++) {
             String propName = "maio_" + mId;
             String strVal = getStringValue(i, propName, element);
             if (strVal != null && !"default".equalsIgnoreCase(strVal)) {
                 try {
                     Integer val = Integer.parseInt(strVal);
                     channalGr.setProperty(propName, val);
                 } catch (NumberFormatException e) {
                     // do nothing
                 }
             }
         }
     }
 
     /**
      * Gets the string value.
      * 
      * @param num the num
      * @param header the header
      * @param element the element
      * @return the string value
      */
     private String getStringValue(int num, String header, NetworkConfigurationTransferData element) {
         int ind = -1;
         for (int i = 0; i < element.getHeaders().length; i++) {
             if (element.getHeaders()[i].equals(header)) {
                 if (++ind == num) {
                     String value = element.getValuesData()[i];
                     return value.equalsIgnoreCase("NULL") ? null : value;
                 }
             }
         }
         return null;
     }
 
     /**
      * Save bsm line.
      * 
      * @param element the element
      */
     private void saveBSMLine(NetworkConfigurationTransferData element) {
         switch (element.getMode()) {
         case TG:
             String tgfull = element.getTg();
             Matcher matcher = tgPat.matcher(tgfull);
             if (!matcher.find(0)) {
                 error(String.format("Incorrect TG format: %s", tgfull));
                 return;
             }
             tgProperty.put(matcher.group(3), element.getFhop());
             return;
 
         default:
             // trx
             Node sector = networkService.findSector(rootNode, null, null, element.getCell(), true);
             if (sector == null) {
                 if (sectorsNotFound++ < 10) {
                     error(String.format("Line %s: Sector with name %s not found", element.getLine(), element.getCell()));
                 }
                 return;
             }
             tgfull = element.getTg();
             matcher = trxPat.matcher(tgfull);
             if (!matcher.find(0)) {
                 error(String.format("Incorrect TG format: %s", tgfull));
                 return;
             }
             String tg = matcher.group(3);
             String trxId = matcher.group(5);
             Integer channelGr = element.getChGroup();
             NodeResult trx = networkService.getTRXNode(sector, trxId, channelGr);
             if (trx.isCreated()) {
                 statistic.updateTypeCount(rootname, NodeTypes.TRX.getId(), 1);
             }
             updateTx(1, 1);
             Node channalGr = networkService.getChannelGroupNode(sector, channelGr);
             updateProperty(rootname, NodeTypes.TRX.getId(), trx, "band", channalGr.getProperty("band", null));
             int hoptype;
             String fchop = tgProperty.get(tg);
             //LN, 15.03.2011, update channel group Node with fhop
             channalGr.setProperty(tg, fchop);
             if ("ON".equalsIgnoreCase((String)channalGr.getProperty("hop", null))) {
                 hoptype = "BB".equalsIgnoreCase(fchop) ? 1 : 2;
             } else {
                 hoptype = 0;
             }
             updateProperty(rootname, NodeTypes.TRX.getId(), trx, "hopping_type", hoptype);
             updateProperty(rootname, NodeTypes.TRX.getId(), trx, "band", channalGr.getProperty("band", null));
             boolean isBcch = 0 == channelGr && "0".equals(trxId);
             updateProperty(rootname, NodeTypes.TRX.getId(), trx, "bcch", isBcch);
             NodeResult plan = getFreqPlan(ORIGINAL).getPlanNode(trx);
             if (plan.isCreated()) {
                 statistic.updateTypeCount(ORIGINAL, NodeTypes.FREQUENCY_PLAN.getId(), 1);
             }
             updateProperty(rootname, NodeTypes.TRX.getId(), trx, "hsn", channalGr.getProperty("hsn", null));
             Integer bcchno = (Integer)sector.getProperty("bcch", null);
             if (!plan.hasProperty("bcc")) {
                 Object bcc = sector.getProperty("bcc");
                 if (bcc != null) {
                     updateProperty(ORIGINAL, NodeTypes.FREQUENCY_PLAN.getId(), plan, "bcc", bcc);
                 }
                 Object ncc = sector.getProperty("ncc");
                 if (ncc != null) {
                     updateProperty(ORIGINAL, NodeTypes.FREQUENCY_PLAN.getId(), plan, "ncc", ncc);
                 }
                 // if (bcchno!=null){
                 // updateProperty(ORIGINAL, NodeTypes.FREQUENCY_PLAN.getId(),plan,"bcch",bcchno);
                 // }
                 // Kasnitskij_V:
                 plan.setProperty(INeoConstants.PROPERTY_NAME_NAME, ORIGINAL);
             }
             if (!plan.hasProperty("arfcn")) {
                 Integer arfcn = null;
                 if (isBcch) {
                     if (bcchno != null) {
 
                         arfcn = bcchno;
                     }
                 } else {
                     int[] dchno = (int[])channalGr.getProperty("dchno", null);
                     if (dchno != null) {
                         if (hoptype < 2 && bcchno != null) {
                             int j = -1;
                             for (Relationship rel : sector
                                    .getRelationships(DatasetRelationshipTypes.PLAN_ENTRY, Direction.OUTGOING)) {
                                 final Node trxOth = rel.getOtherNode(sector);
                                 if (trx.equals(trxOth)) {
                                     continue;
                                 }
                                if (channelGr.equals(trxOth.getProperty("group", null))) {
                                     j++;
                                 }
                             }
                             j++;
                             int k = -1;
                             for (int i = 0; i < dchno.length; i++) {
                                 if (dchno[i] != bcchno) {
                                     k++;
                                     if (k == j) {
                                         arfcn = dchno[i];
                                         break;
                                    }
                                 }
                             }
                         } else {
                             int j = -1;
                             int[] arfcnArr = null;
                             Set<Integer> removedArfcn = new HashSet<Integer>();
                             for (Relationship rel : sector
                                     .getRelationships(DatasetRelationshipTypes.PLAN_ENTRY, Direction.OUTGOING)) {
                                 final Node trxOth = rel.getOtherNode(sector);
                                 if (trx.equals(trxOth)) {
                                     continue;
                                 }
                                 if (channelGr.equals(trxOth.getProperty("group", null))) {
                                     if (hoptype != (Integer)trxOth.getProperty("hopping_type", null)) {
                                         Integer arfcnOth = (Integer)trxOth.getProperty("arfcn", null);
                                         if (arfcnOth != null) {
                                             removedArfcn.add(arfcnOth);
                                         }
                                     }
                                     j++;
                                 }
                             }
                             j++;
                             if (arfcnArr == null) {
                                 arfcnArr = exluded(dchno, removedArfcn);
                                 for (int i = 0; i < arfcnArr.length; i++) {
                                     statistic.indexValue(ORIGINAL, NodeTypes.FREQUENCY_PLAN.getId(), "arfcn", arfcnArr[i]);
                                 }
                             }
                             plan.setProperty("arfcnArr", arfcnArr);
                         }
                     }
                 }
                 if (arfcn != null) {
                     updateProperty(ORIGINAL, NodeTypes.FREQUENCY_PLAN.getId(), plan, "arfcn", arfcn);
                 }
             }
             if (!plan.hasProperty("maio") && hoptype == 2) {
                 String maioPr = "maio_" + trxId;
                 Integer maioInt = (Integer)channalGr.getProperty(maioPr, null);
 
                 if (maioInt == null) {
                     int[] arfcnArr = (int[])plan.getProperty("arfcnArr", null);
                     if (arfcnArr != null) {
                         int maxVal = arfcnArr.length;
                         int numTrx = Integer.valueOf(trxId);
                         if (channelGr == 0) {
                             numTrx++;
                         }
                         if (numTrx > maxVal) {
                             error("Can't create maio property for trx with id=" + trxId);
                         } else {
                             maxVal--;
                             int oddVal = (numTrx - 1) * 2;
                             // TODO check formula
                             maioInt = oddVal <= maxVal ? oddVal : numTrx - (maxVal + 1) / 2;
                         }
                     }
                 }
                 updateProperty(ORIGINAL, NodeTypes.FREQUENCY_PLAN.getId(), plan, "maio", maioInt);
             }
             break;
         }
     }
 
     /**
      * @param dchno
      * @param removedArfcn
      * @return
      */
     private int[] exluded(int[] dchno, Set<Integer> removedArfcn) {
         if (removedArfcn.isEmpty() || dchno.length == 0) {
             return dchno;
         }
         List<Integer> result = new ArrayList<Integer>();
         for (int i = 0; i < dchno.length; i++) {
             if (removedArfcn.contains(dchno[i])) {
                 continue;
             }
             result.add(dchno[i]);
         }
         int[] res = new int[result.size()];
         for (int i = 0; i < res.length; i++) {
             res[i] = result.get(i);
         }
         return res;
     }
 
     /**
      * Gets the freq plan.
      * 
      * @param planName the plan name
      * @return the freq plan
      */
     private FrequencyPlanModel getFreqPlan(String planName) {
         if (freqPlan == null) {
             freqPlan = networkModel.getFrequencyModel(planName);
         }
         return freqPlan;
     }
 
     @Override
     public Iterable<MetaData> getMetaData() {
         return Arrays.asList(new MetaData[] {metadata});
     }
 
     @Override
     public boolean beforeSaveNewElement(NetworkConfigurationTransferData element) {
         tgProperty.clear();
         type = element.getType();
         if (type == NetworkConfigurationFileTypes.BSM) {
             String bscName = element.getBsc();
             if (StringUtils.isEmpty(bscName)) {
                 return true;
             }
             bsc = networkService.findBscNode(rootNode, bscName);
             if (bsc == null) {
                 error(String.format("BSC with name %s not found", bscName));
                 return true;
             }
 
         }
         return false;
     }
 
     @Override
     public void finishUp(NetworkConfigurationTransferData element) {
         initProgress(element);
         if (!haveBsm) {
             createTRXForChannel();
         }
         createFakeBSC();
         fire(0.2d, "Commit data");
         if (sectorsNotFound > 0) {
             error(String.format("%d sectors were not found", sectorsNotFound));
         }
         if (neighboursNotFound > 0) {
             error(String.format("%d neighbours were not found", neighboursNotFound));
         }
         super.finishUp(element);
     }
 
     /**
      * Creates the trx for channel.
      */
     private void createTRXForChannel() {
         // TODO implement;
         Traverser tr = Traversal.description().depthFirst().uniqueness(Uniqueness.NONE).evaluator(new Evaluator() {
 
             @Override
             public Evaluation evaluate(Path arg0) {
                 boolean isSector = NodeTypes.SECTOR.checkNode(arg0.endNode());
                 boolean continues = !isSector;
                 boolean includes = isSector && networkService.haveChanelGroup(arg0.endNode());
                 return Evaluation.of(includes, continues);
             }
         }).relationships(GeoNeoRelationshipTypes.CHILD, Direction.OUTGOING).traverse(rootNode);
         for (Node sector : tr.nodes()) {
             for (Relationship rel : networkService.getChannelGroups(sector)) {
                 createTRXforGroup(sector, rel.getOtherNode(sector));
             }
         }
     }
 
     /**
      * Creates the tr xfor group.
      * 
      * @param sector the sector
      * @param otherNode the other node
      */
     private void createTRXforGroup(Node sector, Node group) {
         String grNum = (String)group.getProperty(INeoConstants.PROPERTY_NAME_NAME, null);
         if (grNum == null) {
             info("Node " + group + "\tgroup do not have number ");
             return;
         }
         Integer num = Integer.parseInt(grNum);
         String hop = (String)group.getProperty("hop", null);
         if (hop == null) {
             info("Node " + group + "\tgroup do not have 'hop' ");
             return;
         }
         int hoptype;
         if ("ON".equalsIgnoreCase((String)group.getProperty("hop", null))) {
             hoptype = 1;
         } else {
             hoptype = 0;
         }
         int[] dchno = (int[])group.getProperty("dchno", null);
         Integer bcchno = (Integer)sector.getProperty("bcch", null);
         if (dchno == null || dchno.length == 0) {
             if (0 == num && bcchno != null) {
                 createTRX(sector, group, num, bcchno, hoptype, 0, bcchno);
             }
             return;
         }
         int id = 0;
         for (int arfcn : dchno) {
             createTRX(sector, group, num, bcchno, hoptype, ++id, arfcn);
         }
     }
 
     /**
      * @param group
      * @param num
      * @param bcchno
      * @param hoptype
      * @param id
      * @param arfcn
      */
     private void createTRX(Node sector, Node group, Integer channelGr, Integer bcchno, int hoptype, int id, int arfcn) {
         if (channelGr == 0 && bcchno != null && bcchno.equals(arfcn)) {
             if (networkService.findTrxNode(sector, "0", channelGr) == null) {
                 id = 0;
             } else {
                 // skip arfcn
                 return;
             }
         }
         NodeResult trx = networkService.getTRXNode(sector, String.valueOf(id), channelGr);
         if (trx.isCreated()) {
             statistic.updateTypeCount(rootname, NodeTypes.TRX.getId(), 1);
             updateTx(1, 1);
         } else {
             return;
         }
         updateProperty(rootname, NodeTypes.TRX.getId(), trx, "band", group.getProperty("band", null));
         updateProperty(rootname, NodeTypes.TRX.getId(), trx, "hopping_type", hoptype);
         updateProperty(rootname, NodeTypes.TRX.getId(), trx, "band", group.getProperty("band", null));
         boolean isBcch = 0 == channelGr && 0 == id;
         updateProperty(rootname, NodeTypes.TRX.getId(), trx, "bcch", isBcch);
         NodeResult plan = getFreqPlan(ORIGINAL).getPlanNode(trx);
         statistic.updateTypeCount(ORIGINAL, NodeTypes.FREQUENCY_PLAN.getId(), 1);
         updateProperty(rootname, NodeTypes.TRX.getId(), trx, "hsn", group.getProperty("hsn", null));
         Object bcc = sector.getProperty("bcc");
         if (bcc != null) {
             updateProperty(ORIGINAL, NodeTypes.FREQUENCY_PLAN.getId(), plan, "bcc", bcc);
         }
         Object ncc = sector.getProperty("ncc");
         if (ncc != null) {
             updateProperty(ORIGINAL, NodeTypes.FREQUENCY_PLAN.getId(), plan, "ncc", ncc);
         }
         // if (bcchno!=null){
         // updateProperty(ORIGINAL, NodeTypes.FREQUENCY_PLAN.getId(),plan,"bcch",bcchno);
         // }
         // Kasnitskij_V:
         plan.setProperty(INeoConstants.PROPERTY_NAME_NAME, ORIGINAL);
         updateProperty(ORIGINAL, NodeTypes.FREQUENCY_PLAN.getId(), plan, "arfcn", arfcn);
         String maioPr = "maio_" + id;
         Integer maioInt = (Integer)group.getProperty(maioPr, null);
         updateProperty(ORIGINAL, NodeTypes.FREQUENCY_PLAN.getId(), plan, "maio", maioInt);
     }
 
     /**
      * Creates the fake bsc.
      */
     private void createFakeBSC() {
         fire(0d, "create FAKE_BSC");
         Traverser tr = Traversal.description().depthFirst().uniqueness(Uniqueness.NONE).evaluator(new Evaluator() {
 
             @Override
             public Evaluation evaluate(Path arg0) {
                 boolean continues = arg0.length() < 1;
                 boolean includes = arg0.length() == 1 && ds.getNodeType(arg0.endNode()) == NodeTypes.SITE;
                 return Evaluation.of(includes, continues);
             }
         }).relationships(GeoNeoRelationshipTypes.CHILD, Direction.OUTGOING).traverse(rootNode);
         Set<Node> fakeSites = new HashSet<Node>();
         Set<Node> removedSites = new HashSet<Node>();
         for (Node site : tr.nodes()) {
             if (site.hasRelationship(GeoNeoRelationshipTypes.CHILD, Direction.OUTGOING)) {
                 fakeSites.add(site);
             } else {
                 removedSites.add(site);
             }
         }
         fire(0.1d, "Move sectors");
         if (!fakeSites.isEmpty()) {
             NodeResult fakeBSC = networkService.getBscNode(rootNode, "Fake BSC", rootNode);
             for (Node site : fakeSites) {
                 Relationship rel = site.getSingleRelationship(GeoNeoRelationshipTypes.CHILD, Direction.INCOMING);
                 rel.delete();
                 fakeBSC.createRelationshipTo(site, GeoNeoRelationshipTypes.CHILD);
                 fire(0.4d / fakeSites.size(), "Move sectors");
             }
 
         } else {
             fire(0.4d, "Move sectors");
         }
         if (!removedSites.isEmpty()) {
             for (Node site : removedSites) {
                 ds.deleteNode(site);
                 fire(0.3d / removedSites.size(), "delete duplication");
             }
         } else {
             fire(0.3d, "delete duplication");
         }
 
     }
 
     @Override
     public void finishSaveNewElement(NetworkConfigurationTransferData element) {
     }
 
     @Override
     protected void fillRootNode(Node rootNode, NetworkConfigurationTransferData element) {
     }
 
     @Override
     protected String getRootNodeType() {
         return NodeTypes.NETWORK.getId();
     }
 
     @Override
     protected String getTypeIdForGisCount(GisProperties gis) {
         return NodeTypes.SITE.getId();
     }
 
 }
