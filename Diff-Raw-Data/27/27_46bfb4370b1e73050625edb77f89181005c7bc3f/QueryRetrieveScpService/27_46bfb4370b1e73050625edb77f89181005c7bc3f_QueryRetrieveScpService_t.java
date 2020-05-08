 /* ***** BEGIN LICENSE BLOCK *****
  * Version: MPL 1.1/GPL 2.0/LGPL 2.1
  *
  * The contents of this file are subject to the Mozilla Public License Version
  * 1.1 (the "License"); you may not use this file except in compliance with
  * the License. You may obtain a copy of the License at
  * http://www.mozilla.org/MPL/
  *
  * Software distributed under the License is distributed on an "AS IS" basis,
  * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
  * for the specific language governing rights and limitations under the
  * License.
  *
  * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
  * Java(TM), available at http://sourceforge.net/projects/dcm4che.
  *
  * The Initial Developer of the Original Code is
  * TIANI Medgraph AG.
  * Portions created by the Initial Developer are Copyright (C) 2003-2005
  * the Initial Developer. All Rights Reserved.
  *
  * Contributor(s):
  * Gunter Zeilinger <gunter.zeilinger@tiani.com>
  * Franz Willer <franz.willer@gwi-ag.com>
  *
  * Alternatively, the contents of this file may be used under the terms of
  * either the GNU General Public License Version 2 or later (the "GPL"), or
  * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
  * in which case the provisions of the GPL or the LGPL are applicable instead
  * of those above. If you wish to allow use of your version of this file only
  * under the terms of either the GPL or the LGPL, and not to allow others to
  * use your version of this file under the terms of the MPL, indicate your
  * decision by deleting the provisions above and replace them with the notice
  * and other provisions required by the GPL or the LGPL. If you do not delete
  * the provisions above, a recipient may use your version of this file under
  * the terms of any one of the MPL, the GPL or the LGPL.
  *
  * ***** END LICENSE BLOCK ***** */
 
 package org.dcm4chex.archive.dcm.qrscp;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.sql.Types;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.Map.Entry;
 
 import javax.ejb.FinderException;
 import javax.management.InstanceNotFoundException;
 import javax.management.JMException;
 import javax.management.MBeanException;
 import javax.management.ObjectName;
 import javax.management.ReflectionException;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.dcm4che.auditlog.InstancesAction;
 import org.dcm4che.auditlog.RemoteNode;
 import org.dcm4che.data.Command;
 import org.dcm4che.data.Dataset;
 import org.dcm4che.data.DcmObjectFactory;
 import org.dcm4che.dict.DictionaryFactory;
 import org.dcm4che.dict.Status;
 import org.dcm4che.dict.Tags;
 import org.dcm4che.dict.UIDDictionary;
 import org.dcm4che.dict.UIDs;
 import org.dcm4che.net.AcceptorPolicy;
 import org.dcm4che.net.ActiveAssociation;
 import org.dcm4che.net.Association;
 import org.dcm4che.net.AssociationFactory;
 import org.dcm4che.net.DataSource;
 import org.dcm4che.net.DcmServiceException;
 import org.dcm4che.net.DcmServiceRegistry;
 import org.dcm4che.net.Dimse;
 import org.dcm4che.net.ExtNegotiator;
 import org.dcm4che.net.PresContext;
 import org.dcm4che2.audit.message.AuditMessage;
 import org.dcm4che2.audit.message.InstancesTransferredMessage;
 import org.dcm4che2.audit.message.ParticipantObjectDescription;
 import org.dcm4che2.audit.util.InstanceSorter;
 import org.dcm4cheri.util.StringUtils;
 import org.dcm4chex.archive.common.DatasetUtils;
 import org.dcm4chex.archive.config.RetryIntervalls;
 import org.dcm4chex.archive.dcm.AbstractScpService;
 import org.dcm4chex.archive.ejb.interfaces.AEDTO;
 import org.dcm4chex.archive.ejb.interfaces.FileDTO;
 import org.dcm4chex.archive.ejb.interfaces.FileSystemMgt2;
 import org.dcm4chex.archive.ejb.interfaces.FileSystemMgt2Home;
 import org.dcm4chex.archive.ejb.jdbc.FileInfo;
 import org.dcm4chex.archive.ejb.jdbc.QueryCmd;
 import org.dcm4chex.archive.ejb.jdbc.RetrieveCmd;
 import org.dcm4chex.archive.exceptions.ConfigurationException;
 import org.dcm4chex.archive.exceptions.NoPresContextException;
 import org.dcm4chex.archive.exceptions.UnknownAETException;
 import org.dcm4chex.archive.factory.ScpFactoryUtil;
 import org.dcm4chex.archive.mbean.DicomSecurityDelegate;
 import org.dcm4chex.archive.mbean.TLSConfigDelegate;
 import org.dcm4chex.archive.perf.PerfMonDelegate;
 import org.dcm4chex.archive.perf.PerfPropertyEnum;
 import org.dcm4chex.archive.util.EJBHomeFactory;
 import org.dcm4chex.archive.util.FileDataSource;
 import org.dcm4chex.archive.util.FileUtils;
 import org.jboss.logging.Logger;
 
 /**
  * @author Gunter.Zeilinger@tiani.com
  * @version $Revision: 8250 $ $Date: 2008-11-20 16:44:18 +0100 (Thu, 20 Nov 2008) $
  * @since 31.08.2003
  */
 public class QueryRetrieveScpService extends AbstractScpService {
 
     private static final String ANY = "ANY";
 
     private static final String NONE = "NONE";
 
     private static final String[] EMPTY = {};
 
     private static final String SEND_BUFFER = "SEND_BUFFER";
 
     private static final Timer pendingRspTimer = new Timer(true);
 
     static final UIDDictionary uidDict = 
             DictionaryFactory.getInstance().getDefaultUIDDictionary();
 
     private String[] sendNoPixelDataToAETs = null;
 
     private String[] offerNoPixelDataToAETs = null;
 
     private String[] offerNoPixelDataDeflateToAETs = null;
 
     private String[] sendWithDefaultTransferSyntaxToAETs = null;
 
     private String[] ignoreUnsupportedSOPClassFailuresByAETs = null;
     
     private String[] unrestrictedQueryPermissionsToAETitles = null;
 
     private String[] unrestrictedReadPermissionsToAETitles = null;
 
     private String[] unrestrictedExportPermissionsToAETitles = null;
 
     private String[] hideWithoutIssuerOfPIDFromAETs;
 
     private boolean invertHideWithoutIssuerOfPIDFromAETs;
 
     private Map ignorableSOPClasses = new LinkedHashMap();
 
     private LinkedHashMap requestStgCmtFromAETs = new LinkedHashMap();
 
     private ObjectName stgCmtScuScpName;
 
     private ObjectName tarRetrieverName;
 
     private ObjectName pixQueryServiceName;
     
     private TLSConfigDelegate tlsConfig = new TLSConfigDelegate(this);
 
     private DicomSecurityDelegate dicomSecurity =
             new DicomSecurityDelegate(this);
 
     private boolean sendPendingRetrieveRSP = true;
 
     private long pendingRetrieveRSPInterval = 5000;
     
     private boolean forwardAsMoveOriginator = true;
 
     private String directForwardingList="";
 
     private Map<String,List<String>> directForwardingMap =
             new HashMap<String, List<String>>();
 
     private String localStorageAET;
 
     private boolean recordStudyAccessTime = true;
 
     private boolean noMatchForNoValue = true;
 
     private boolean checkMatchingKeySupported = true;
 
     private String[] pixQueryCallingAETs;
     
     private String[] pixQueryIssuers;
     
     private String pixQueryDefIssuer;
     
     private boolean coerceRequestPatientIds;
 
     private String[] coerceRequestPatientIdsAETs;
 
     private int acTimeout = 5000;
 
     private int dimseTimeout = 0;
 
     private int soCloseDelay = 500;
 
     private int maxStoreOpsInvoked = 0;
 
     private FindScp dicomFindScp = null;
 
     private FindScp tianiFindScp = new FindScp(this, false);
 
     private FindScp tianiBlockedFindScp = new BlockedFindScp(this);
 
     private FindScp tianiVMFFindScp = new VMFFindScp(this);
 
     private MoveScp moveScp = null;
 
     private GetScp getScp = null;
 
     private int maxUIDsPerMoveRQ = 100;
 
     private int maxBlockedFindRSP = 10000;
 
     private int bufferSize = 8192;
 
     private File virtualEnhancedCTConfigFile;
 
     private Dataset virtualEnhancedCTConfig;
 
     private File virtualEnhancedMRConfigFile;
 
     private Dataset virtualEnhancedMRConfig;
 
     /**
      * Map containing accepted SOP Class UIDs. key is name (as in config
      * string), value is real uid)
      */
     private Map standardCuidMap = new LinkedHashMap();
 
     /**
      * Map containing accepted private SOP Class UIDs. key is name (as in config
      * string), value is real uid)
      */
     private Map privateCuidMap = new LinkedHashMap();
 
     /**
      * Map containing accepted Transfer Syntax UIDs for private SOP Classes. key
      * is name (as in config string), value is real uid)
      */
     private Map privateTSuidMap = new LinkedHashMap();
 
     public QueryRetrieveScpService() {
     	moveScp = createMoveScp();
         getScp = createGetScp();
     	dicomFindScp = createFindScp();
     }
     
     DicomSecurityDelegate dicomSecurity() {
         return dicomSecurity;
     }
     
     protected MoveScp createMoveScp() {
         return new MoveScp(this);
     }
 
     protected GetScp createGetScp() {
         return new GetScp(this);
     }
 
     protected FindScp createFindScp() {
         return new FindScp(this, true);
     }
 	
     public final String getVirtualEnhancedCTConfigFile() {
         return virtualEnhancedCTConfigFile.getPath();
     }
 
     public final void setVirtualEnhancedCTConfigFile(String path) {
         this.virtualEnhancedCTConfigFile = new File(path.replace('/',
                 File.separatorChar));
     }
 
     public final String getVirtualEnhancedMRConfigFile() {
         return virtualEnhancedMRConfigFile.getPath();
     }
 
     public final void setVirtualEnhancedMRConfigFile(String path) {
         this.virtualEnhancedMRConfigFile = new File(path.replace('/',
                 File.separatorChar));
     }
 
     public final ObjectName getTLSConfigName() {
         return tlsConfig.getTLSConfigName();
     }
 
     public final void setTLSConfigName(ObjectName tlsConfigName) {
         tlsConfig.setTLSConfigName(tlsConfigName);
     }
 
     public final ObjectName getDicomSecurityServiceName() {
         return dicomSecurity.getDicomSecurityServiceName();
     }
 
     public final void setDicomSecurityServiceName(ObjectName serviceName) {
         this.dicomSecurity.setDicomSecurityServiceName(serviceName);
     }
     
     public final int getReceiveBufferSize() {
         return tlsConfig.getReceiveBufferSize();
     }
 
     public final void setReceiveBufferSize(int size) {
         tlsConfig.setReceiveBufferSize(size);
     }
 
     public final int getSendBufferSize() {
         return tlsConfig.getSendBufferSize();
     }
 
     public final void setSendBufferSize(int size) {
         tlsConfig.setSendBufferSize(size);
     }
 
     public final boolean isTcpNoDelay() {
         return tlsConfig.isTcpNoDelay();
     }
 
     public final void setTcpNoDelay(boolean on) {
         tlsConfig.setTcpNoDelay(on);
     }
 
     public final ObjectName getStgCmtScuScpName() {
         return stgCmtScuScpName;
     }
 
     public final void setStgCmtScuScpName(ObjectName stgCmtScuScpName) {
         this.stgCmtScuScpName = stgCmtScuScpName;
     }
 
     public final ObjectName getTarRetrieverName() {
         return tarRetrieverName;
     }
 
     public final void setTarRetrieverName(ObjectName tarRetrieverName) {
         this.tarRetrieverName = tarRetrieverName;
     }
 
     public final ObjectName getPixQueryServiceName() {
         return pixQueryServiceName;
     }
 
     public final void setPixQueryServiceName(ObjectName name) {
         this.pixQueryServiceName = name;
     }
     
     public String getPixQueryCallingAETs() {
         return pixQueryCallingAETs == null ? ANY
                 : pixQueryCallingAETs.length == 0 ? NONE
                 : StringUtils.toString(pixQueryCallingAETs, '\\');
     }
 
     public void setPixQueryCallingAETs(String s) {
         String trim = s.trim();
         this.pixQueryCallingAETs = trim.equalsIgnoreCase(ANY) ? null
                 : trim.equalsIgnoreCase(NONE) ? EMPTY
                 : StringUtils.split(trim, '\\');
     }
     
     boolean isPixQueryCallingAET(String aet) {
         return pixQueryCallingAETs == null
             || Arrays.asList(pixQueryCallingAETs).contains(aet);
     }
 
     public String getPixQueryIssuers() {
         return pixQueryIssuers == null ? ANY
                 : StringUtils.toString(pixQueryIssuers, ',');
     }
 
     public void setPixQueryIssuers(String s) {
         String trim = s.trim();
         this.pixQueryIssuers = trim.equalsIgnoreCase(ANY) ? null
                 : StringUtils.split(trim, ',');
     }
 
     boolean isPixQueryIssuer(String issuer) {
         return pixQueryIssuers == null
             || Arrays.asList(pixQueryIssuers).contains(issuer);
     }
     
     public final String getPixQueryDefIssuer() {
         return pixQueryDefIssuer;
     }
 
     public final void setPixQueryDefIssuer(String pixQueryDefIssuer) {
         this.pixQueryDefIssuer = pixQueryDefIssuer;
     }
 
     public final boolean isCoerceRequestPatientIds() {
         return coerceRequestPatientIds;
     }
 
     public final void setCoerceRequestPatientIds(boolean coerceRequestPatientIds) {
         this.coerceRequestPatientIds = coerceRequestPatientIds;
     }
 
     public String getCoerceRequestPatientIdsAETs() {
         return coerceRequestPatientIdsAETs == null ? ANY
                 : StringUtils.toString(coerceRequestPatientIdsAETs, '\\');
     }
 
     public void setCoerceRequestPatientIdsAETs(String s) {
         String trim = s.trim();
         this.coerceRequestPatientIdsAETs = trim.equalsIgnoreCase(ANY) ? null
                 : StringUtils.split(trim, '\\');
     }
 
     public final boolean isCoerceRequestPatientIdsAET(String aet) {
         return isCoerceRequestPatientIds()&&
                 (coerceRequestPatientIdsAETs == null || Arrays.asList(coerceRequestPatientIdsAETs).contains(aet));
     }
     
     public final String getUnrestrictedQueryPermissionsToAETitles() {
         return unrestrictedQueryPermissionsToAETitles == null ? ANY
                 : StringUtils.toString(
                         unrestrictedQueryPermissionsToAETitles, '\\');
     }
 
     public final void setUnrestrictedQueryPermissionsToAETitles(String s) {
         String trim = s.trim();
         this.unrestrictedQueryPermissionsToAETitles = 
                 trim.equalsIgnoreCase(ANY) ? null 
                         : StringUtils.split(trim, '\\');
     }
 
     final boolean hasUnrestrictedQueryPermissions(String aet) {
         return unrestrictedQueryPermissionsToAETitles == null 
                 || Arrays.asList(unrestrictedQueryPermissionsToAETitles)
                         .contains(aet);
     }
         
     public final String getUnrestrictedReadPermissionsToAETitles() {
         return unrestrictedReadPermissionsToAETitles == null ? ANY
                 : StringUtils.toString(
                         unrestrictedReadPermissionsToAETitles, '\\');
     }
 
     public final void setUnrestrictedReadPermissionsToAETitles(String s) {
         String trim = s.trim();
         this.unrestrictedReadPermissionsToAETitles = 
                 trim.equalsIgnoreCase(ANY) ? null 
                         : StringUtils.split(trim, '\\');
     }
 
     final boolean hasUnrestrictedReadPermissions(String aet) {
         return unrestrictedReadPermissionsToAETitles == null 
                 || Arrays.asList(unrestrictedReadPermissionsToAETitles)
                         .contains(aet);
     }
         
     public final String getUnrestrictedExportPermissionsToAETitles() {
         return unrestrictedExportPermissionsToAETitles == null ? ANY
                 : StringUtils.toString(
                         unrestrictedExportPermissionsToAETitles, '\\');
     }
 
     public final void setUnrestrictedExportPermissionsToAETitles(String s) {
         String trim = s.trim();
         this.unrestrictedExportPermissionsToAETitles = 
                 trim.equalsIgnoreCase(ANY) ? null 
                         : StringUtils.split(trim, '\\');
     }
 
     final boolean hasUnrestrictedExportPermissions(String aet) {
         return unrestrictedExportPermissionsToAETitles == null 
                 || Arrays.asList(unrestrictedExportPermissionsToAETitles)
                         .contains(aet);
     }
 
     public final String getHideWithoutIssuerOfPatientIDFromAETs() {
         return invertHideWithoutIssuerOfPIDFromAETs ? "!\\" : ""
             + (hideWithoutIssuerOfPIDFromAETs == null ? "NONE"
                     : StringUtils.toString(
                             hideWithoutIssuerOfPIDFromAETs, '\\'));
     }
 
     public final void setHideWithoutIssuerOfPatientIDFromAETs(String aets) {
         if (invertHideWithoutIssuerOfPIDFromAETs = aets.startsWith("!\\")) {
             aets = aets.substring(2);
         }
         hideWithoutIssuerOfPIDFromAETs = aets.equalsIgnoreCase("NONE")
                 ? null : StringUtils.split(aets, '\\');
     }
 
     boolean isHideWithoutIssuerOfPIDFromAET(String callingAET) {
         if (hideWithoutIssuerOfPIDFromAETs != null) {
             for (String aet : hideWithoutIssuerOfPIDFromAETs) {
                 if (aet.equals(callingAET)) {
                     return !invertHideWithoutIssuerOfPIDFromAETs;
                 }
             }
         }
         return invertHideWithoutIssuerOfPIDFromAETs;
     }
 
     public final boolean isNoMatchForNoValue() {
         return noMatchForNoValue;
     }
 
     public final void setNoMatchForNoValue(boolean noMatchForNoValue) {
         this.noMatchForNoValue = noMatchForNoValue;
     }
 
     /**
      * @return Returns the checkMatchingKeySupport.
      */
     public boolean isCheckMatchingKeySupported() {
         return checkMatchingKeySupported;
     }
 
     /**
      * @param checkMatchingKeySupport
      *            The checkMatchingKeySupport to set.
      */
     public void setCheckMatchingKeySupported(boolean checkMatchingKeySupport) {
         this.checkMatchingKeySupported = checkMatchingKeySupport;
     }
 
     public final boolean getLazyFetchSeriesAttrsOnImageLevelQuery() {
         return QueryCmd.lazyFetchSeriesAttrsOnImageLevelQuery;
     }
 
     public final void setLazyFetchSeriesAttrsOnImageLevelQuery(boolean enable) {
         QueryCmd.lazyFetchSeriesAttrsOnImageLevelQuery = enable;
     }
 
     public final boolean getCacheSeriesAttrsOnImageLevelQuery() {
         return QueryCmd.cacheSeriesAttrsOnImageLevelQuery ||
                 QueryCmd.lazyFetchSeriesAttrsOnImageLevelQuery;
     }
 
     public final void setCacheSeriesAttrsOnImageLevelQuery(boolean enable) {
         QueryCmd.cacheSeriesAttrsOnImageLevelQuery = enable;
     }
 
     public final boolean getAccessBlobAsLongVarBinaryOnQuery() {
         return QueryCmd.blobAccessType == Types.LONGVARBINARY;
     }
 
     public final void setAccessBlobAsLongVarBinaryOnQuery(boolean enable) {
         QueryCmd.blobAccessType = enable ? Types.LONGVARBINARY : Types.BLOB;
     }
 
     public final boolean getAccessSeriesBlobAsLongVarBinaryOnImageLevelQuery() {
         return QueryCmd.seriesBlobAccessType == Types.LONGVARBINARY;
     }
 
     public final void setAccessSeriesBlobAsLongVarBinaryOnImageLevelQuery(boolean enable) {
         QueryCmd.seriesBlobAccessType = enable ? Types.LONGVARBINARY : Types.BLOB;
     }
 
     public final boolean getLazyFetchSeriesAttrsOnRetrieve() {
         return RetrieveCmd.lazyFetchSeriesAttrs;
     }
 
     public final void setLazyFetchSeriesAttrsOnRetrieve(boolean enable) {
         RetrieveCmd.lazyFetchSeriesAttrs = enable;
     }
 
     public final boolean getCacheSeriesAttrsOnRetrieve() {
         return RetrieveCmd.cacheSeriesAttrs || RetrieveCmd.lazyFetchSeriesAttrs;
     }
 
     public final void setCacheSeriesAttrsOnRetrieve(boolean enable) {
         RetrieveCmd.cacheSeriesAttrs = enable;
     }
 
     public final int getCacheSeriesAttrsOnRetrieveMaxSize() {
         return RetrieveCmd.getSeriesAttrsCacheMaxSize();
     }
 
     public final void setCacheSeriesAttrsOnRetrieveMaxSize(int maxSize) {
         RetrieveCmd.setSeriesAttrsCacheMaxSize(maxSize);
     }
 
     public final String getCacheSeriesAttrsOnRetrieveCurrencyTimeLimit() {
         return RetryIntervalls.formatInterval(
                 RetrieveCmd.seriesAttrsCacheCurrencyTimeLimit);
     }
 
     public final void setCacheSeriesAttrsOnRetrieveCurrencyTimeLimit(String s) {
         RetrieveCmd.seriesAttrsCacheCurrencyTimeLimit =
                 RetryIntervalls.parseInterval(s);
     }
 
     public final boolean getAccessBlobAsLongVarBinaryOnRetrieve() {
         return RetrieveCmd.blobAccessType == Types.LONGVARBINARY;
     }
 
     public final void setAccessBlobAsLongVarBinaryOnRetrieve(boolean enable) {
         RetrieveCmd.blobAccessType = enable ? Types.LONGVARBINARY : Types.BLOB;
     }
 
     public final boolean getAccessSeriesBlobAsLongVarBinaryOnRetrieve() {
         return RetrieveCmd.seriesBlobAccessType == Types.LONGVARBINARY;
     }
 
     public final void setAccessSeriesBlobAsLongVarBinaryOnRetrieve(boolean enable) {
         RetrieveCmd.seriesBlobAccessType = enable ? Types.LONGVARBINARY : Types.BLOB;
     }
 
     public final String getQueryTransactionIsolationLevel() {
         return QueryCmd.transactionIsolationLevelAsString(
                 QueryCmd.transactionIsolationLevel);
     }
 
     public final void setQueryTransactionIsolationLevel(String level) {
         QueryCmd.transactionIsolationLevel =
                 QueryCmd.transactionIsolationLevelOf(level);
     }
 
     public final String getRetrieveTransactionIsolationLevel() {
         return RetrieveCmd.transactionIsolationLevelAsString(
                 RetrieveCmd.transactionIsolationLevel);
     }
 
     public final void setRetrieveTransactionIsolationLevel(String level) {
         RetrieveCmd.transactionIsolationLevel =
                 RetrieveCmd.transactionIsolationLevelOf(level);
     }
     
     public final boolean isRetrieveWithoutLeftJoins() {
         return RetrieveCmd.isNoLeftJoin();
     }
 
     public final void setRetrieveWithoutLeftJoins(boolean noLeftJoin) {
         RetrieveCmd.setNoLeftJoin(noLeftJoin);
     }    
 
     public String getAcceptedPrivateSOPClasses() {
         return toString(privateCuidMap);
     }
 
     public void setAcceptedPrivateSOPClasses(String s) {
         updateAcceptedSOPClass(privateCuidMap, s, null);
     }
 
     public String getAcceptedTransferSyntaxForPrivateSOPClasses() {
         return toString(privateTSuidMap);
     }
 
     public void setAcceptedTransferSyntaxForPrivateSOPClasses(String s) {
         updateAcceptedTransferSyntax(privateTSuidMap, s);
     }
 
     public String getAcceptedStandardSOPClasses() {
         return toString(standardCuidMap);
     }
 
     public void setAcceptedStandardSOPClasses(String s) {
         updateAcceptedSOPClass(standardCuidMap, s, null);
     }
 
     public final int getAcTimeout() {
         return acTimeout;
     }
 
     public final void setAcTimeout(int acTimeout) {
         this.acTimeout = acTimeout;
     }
 
     public final int getDimseTimeout() {
         return dimseTimeout;
     }
 
     public final void setDimseTimeout(int dimseTimeout) {
         this.dimseTimeout = dimseTimeout;
     }
 
     public final int getSoCloseDelay() {
         return soCloseDelay;
     }
 
     public final void setSoCloseDelay(int soCloseDelay) {
         this.soCloseDelay = soCloseDelay;
     }
 
     public final int getMaxStoreOpsInvoked() {
         return maxStoreOpsInvoked;
     }
 
     public final void setMaxStoreOpsInvoked(int maxStoreOpsInvoked) {
         this.maxStoreOpsInvoked = maxStoreOpsInvoked;
     }
 
     public final boolean isSendPendingRetrieveRSP() {
         return sendPendingRetrieveRSP;
     }
 
     public final void setSendPendingRetrieveRSP(boolean sendPendingRetrieveRSP) {
         this.sendPendingRetrieveRSP = sendPendingRetrieveRSP;
     }
 
     public final void setPendingRetrieveRSPInterval(long ms) {
         if (ms <= 0) {
             throw new IllegalArgumentException("pendingRetrieveRSPInterval: " +  ms);
         }
         pendingRetrieveRSPInterval = ms ;
     }
     
     public final long getPendingRetrieveRSPInterval() {
         return pendingRetrieveRSPInterval ;
     }
     
     public final boolean isForwardAsMoveOriginator() {
         return forwardAsMoveOriginator;
     }
 
     public final void setForwardAsMoveOriginator(boolean forwardAsMoveOriginator) {
         this.forwardAsMoveOriginator = forwardAsMoveOriginator;
     }
 
     public final boolean isRecordStudyAccessTime() {
         return recordStudyAccessTime;
     }
 
     public final void setRecordStudyAccessTime(boolean updateAccessTime) {
         this.recordStudyAccessTime = updateAccessTime;
     }
 
     public final String getSendNoPixelDataToAETs() {
         return sendNoPixelDataToAETs == null ? NONE : StringUtils.toString(
                 sendNoPixelDataToAETs, '\\');
     }
 
     public final void setSendNoPixelDataToAETs(String aets) {
         this.sendNoPixelDataToAETs = NONE.equalsIgnoreCase(aets) ? null
                 : StringUtils.split(aets, '\\');
     }
 
     public final String getOfferNoPixelDataToAETs() {
         return offerNoPixelDataToAETs == null ? ANY
                 : offerNoPixelDataToAETs.length == 0 ? NONE
                 : StringUtils.toString(offerNoPixelDataToAETs, '\\');
     }
 
     public final void setOfferNoPixelDataToAETs(String aets) {
         this.offerNoPixelDataToAETs = ANY.equalsIgnoreCase(aets) ? null
                 : NONE.equalsIgnoreCase(aets) ? EMPTY
                 : StringUtils.split(aets, '\\');
     }
 
     public final String getOfferNoPixelDataDeflateToAETs() {
         return offerNoPixelDataDeflateToAETs == null ? ANY
                 : offerNoPixelDataDeflateToAETs.length == 0 ? NONE
                 : StringUtils.toString(offerNoPixelDataDeflateToAETs, '\\');
     }
 
     public final void setOfferNoPixelDataDeflateToAETs(String aets) {
         this.offerNoPixelDataDeflateToAETs = ANY.equalsIgnoreCase(aets) ? null
                 : NONE.equalsIgnoreCase(aets) ? EMPTY
                 : StringUtils.split(aets, '\\');
     }
 
     public final String getSendWithDefaultTransferSyntaxToAETs() {
         return sendWithDefaultTransferSyntaxToAETs == null ? ANY
                 : sendWithDefaultTransferSyntaxToAETs.length == 0 ? NONE
                 : StringUtils.toString(sendWithDefaultTransferSyntaxToAETs,
                         '\\');
     }
 
     public final void setSendWithDefaultTransferSyntaxToAETs(String aets) {
         this.sendWithDefaultTransferSyntaxToAETs = ANY.equalsIgnoreCase(aets) 
                 ? null : NONE.equalsIgnoreCase(aets) ? EMPTY
                         : StringUtils.split(aets, '\\');
     }
 
     public final String getIgnoreUnsupportedSOPClassFailuresByAETs() {
         return ignoreUnsupportedSOPClassFailuresByAETs == null ? NONE
                 : StringUtils.toString(ignoreUnsupportedSOPClassFailuresByAETs,
                         '\\');
     }
 
     public final void setIgnoreUnsupportedSOPClassFailuresByAETs(String aets) {
         this.ignoreUnsupportedSOPClassFailuresByAETs = NONE
                 .equalsIgnoreCase(aets) ? null : StringUtils.split(aets, '\\');
     }
 
     public final String getIgnorableSOPClasses() {
         return toString(ignorableSOPClasses);
     }
 
     public final void setIgnorableSOPClasses(String s) {
         this.ignorableSOPClasses = parseUIDs(s);
     }
 
     public final String getRequestStgCmtFromAETs() {
         if (requestStgCmtFromAETs.isEmpty())
             return NONE;
         StringBuffer sb = new StringBuffer();
         Iterator it = requestStgCmtFromAETs.entrySet().iterator();
         while (it.hasNext()) {
             final Map.Entry entry = (Entry) it.next();
             final String key = (String) entry.getKey();
             final String value = (String) entry.getValue();
             sb.append(key);
             if (!key.equals(value))
                 sb.append(':').append(value);
             sb.append('\\');
         }
         sb.setLength(sb.length() - 1);
         return sb.toString();
     }
 
     public final void setRequestStgCmtFromAETs(String aets) {
         requestStgCmtFromAETs.clear();
         if (aets != null && aets.length() > 0 && !aets.equalsIgnoreCase(NONE)) {
             String[] a = StringUtils.split(aets, '\\');
             String s;
             int c;
             for (int i = 0; i < a.length; i++) {
                 s = a[i];
                 c = s.indexOf(':');
                 if (c == -1)
                     requestStgCmtFromAETs.put(s, s);
                 else if (c > 0 && c < s.length() - 1)
                     requestStgCmtFromAETs.put(s.substring(0, c), s
                             .substring(c + 1));
             }
         }
     }
 
     public final int getMaxUIDsPerMoveRQ() {
         return maxUIDsPerMoveRQ;
     }
 
     public final void setMaxUIDsPerMoveRQ(int max) {
         this.maxUIDsPerMoveRQ = max;
     }
 
     public final void setMaxBlockedFindRSP(int max) {
         this.maxBlockedFindRSP = max;
     }
 
     public final int getMaxBlockedFindRSP() {
         return maxBlockedFindRSP;
     }
 
     public synchronized void setDirectForwardingList(String directForwardingList) {
         if (!this.directForwardingList.equals(directForwardingList)) {
             this.directForwardingList = directForwardingList;
             directForwardingMap.clear();
             String[] mappings = directForwardingList.split("\\;");
             for (String mapping : mappings) {
                 int k = mapping.indexOf(',');
                 String src;
                 String dests;
                 if (k < 0) {
                     dests = "any";
                     src = mapping;
                 } else {
                     dests = mapping.substring(k + 1);
                     src = mapping.substring(0, k).trim();
                     if (dests.equals("") || dests.equals("*")) {
                         dests = "any";
                     }
                 }
                 List<String> lst = new ArrayList<String>();
                 if (!dests.equalsIgnoreCase("any")) {
                     lst.addAll(Arrays.asList(dests.split("\\,")));
                 }
                 directForwardingMap.put(src, lst);
             }
         }
     }
 
     public synchronized String getDirectForwardingList() {
         return directForwardingList;
     }
 
     public synchronized void addDirectForwarding(String aet, String dest) {
         if (dest == null || dest.equals("") || dest.equals("*")) {
             dest = "any";
         }
         List<String> lst = directForwardingMap.get(aet);
         if (lst != null) {
             if (lst.size() == 0 || lst.contains(dest)) {
                 return;
             }
             if (dest.equalsIgnoreCase("any")) {
                 lst.clear();
             } else
                 lst.add(dest);
         } else {
             lst = new ArrayList<String>();
             if (!dest.equalsIgnoreCase("any")) {
                 lst.add(dest);
             }
         }
         setDirectForwardingList(toDirectForwardList());
     }
 
     public synchronized void removeDirectForwarding(String aet) {
         removeDirectForwarding(aet, "any");
     }
 
     public synchronized void removeDirectForwarding(String aet, String dest) {
         if (dest == null || dest.equals("") || dest.equals("*")) {
             dest = "any";
         }
         if (dest.equalsIgnoreCase("any")) {
             directForwardingMap.remove(aet);
         } else {
             List<String> lst = directForwardingMap.get(aet);
             if (lst == null || !lst.contains(dest)) {
                 return;
             }
             if (lst.size() == 1)
                 directForwardingMap.remove(aet);
             else
                 lst.remove(dest);
         }
         setDirectForwardingList(toDirectForwardList());
     }
 
     private String toDirectForwardList() {
         StringBuffer buf = new StringBuffer();
         int i = 0;
         for (String aet : directForwardingMap.keySet()) {
             if (i > 0)
                 buf.append(";");
             List<String> lst = directForwardingMap.get(aet);
             buf.append(aet);
             if (lst.size() == 0) {
                 buf.append(",any");
             } else {
                 for (String dest : lst) {
                     buf.append(",").append(dest);
                 }
             }
         }
         return buf.toString();
     }
 
     public synchronized boolean isDirectForwarding(String retrieveAET,
             String destination) {
         List<String> lst = directForwardingMap.get(retrieveAET);
         if (lst != null) {
             return lst.size() == 0 || lst.contains(destination);
         }
         return false;
     }
 
     public void setLocalStorageAET(String localStorageAET) {
         this.localStorageAET = localStorageAET;
     }
 
     public String getLocalStorageAET() {
         return localStorageAET;
     }
 
     public final ObjectName getPerfMonServiceName() {
 		return dicomFindScp.getPerfMonServiceName();
 	}
 
 	public final void setPerfMonServiceName(ObjectName perfMonServiceName) {
 		dicomFindScp.setPerfMonServiceName(perfMonServiceName);
 		moveScp.setPerfMonServiceName(perfMonServiceName);
 	}
 	
 	public MoveScp getMoveScp() {
 		return moveScp;
 	}
 
     protected void bindDcmServices(DcmServiceRegistry services) {
         services.bind(UIDs.PatientRootQueryRetrieveInformationModelFIND,
                 dicomFindScp);
         services.bind(UIDs.StudyRootQueryRetrieveInformationModelFIND,
                 dicomFindScp);
         services.bind(UIDs.PatientStudyOnlyQueryRetrieveInformationModelFIND,
                 dicomFindScp);
 
         services.bind(UIDs.Dcm4cheStudyRootQueryRetrieveInformationModelFIND,
                 tianiFindScp);
         services.bind(
                 UIDs.Dcm4cheBlockedStudyRootQueryRetrieveInformationModelFIND,
                 tianiBlockedFindScp);
         services
                 .bind(
                         UIDs.Dcm4cheVirtualMultiFrameStudyRootQueryRetrieveInformationModelFIND,
                         tianiVMFFindScp);
 
         services.bind(UIDs.PatientRootQueryRetrieveInformationModelMOVE,
                 moveScp);
         services.bind(UIDs.StudyRootQueryRetrieveInformationModelMOVE,
                 moveScp);
         services.bind(UIDs.PatientStudyOnlyQueryRetrieveInformationModelMOVE,
                 moveScp);
 
         services.bind(UIDs.PatientRootQueryRetrieveInformationModelGET,
                 getScp);
         services.bind(UIDs.StudyRootQueryRetrieveInformationModelGET,
                 getScp);
         services.bind(UIDs.PatientStudyOnlyQueryRetrieveInformationModelGET,
                 getScp);
 
         dcmHandler.addAssociationListener(dicomFindScp);
         dcmHandler.addAssociationListener(moveScp);
     }
 
     protected void unbindDcmServices(DcmServiceRegistry services) {
         services.unbind(UIDs.PatientRootQueryRetrieveInformationModelFIND);
         services.unbind(UIDs.StudyRootQueryRetrieveInformationModelFIND);
         services.unbind(UIDs.PatientStudyOnlyQueryRetrieveInformationModelFIND);
 
         services.unbind(UIDs.Dcm4chePatientRootQueryRetrieveInformationModelFIND);
         services.unbind(UIDs.Dcm4cheStudyRootQueryRetrieveInformationModelFIND);
         services
                 .unbind(UIDs.Dcm4chePatientStudyOnlyQueryRetrieveInformationModelFIND);
 
         services
                 .unbind(UIDs.Dcm4cheBlockedPatientRootQueryRetrieveInformationModelFIND);
         services
                 .unbind(UIDs.Dcm4cheBlockedStudyRootQueryRetrieveInformationModelFIND);
         services
                 .unbind(UIDs.Dcm4cheBlockedPatientStudyOnlyQueryRetrieveInformationModelFIND);
 
         services
                 .unbind(UIDs.Dcm4cheVirtualMultiFramePatientRootQueryRetrieveInformationModelFIND);
         services
                 .unbind(UIDs.Dcm4cheVirtualMultiFrameStudyRootQueryRetrieveInformationModelFIND);
 
         services.unbind(UIDs.PatientRootQueryRetrieveInformationModelMOVE);
         services.unbind(UIDs.StudyRootQueryRetrieveInformationModelMOVE);
         services.unbind(UIDs.PatientStudyOnlyQueryRetrieveInformationModelMOVE);
 
         services.unbind(UIDs.PatientRootQueryRetrieveInformationModelGET);
         services.unbind(UIDs.StudyRootQueryRetrieveInformationModelGET);
         services.unbind(UIDs.PatientStudyOnlyQueryRetrieveInformationModelGET);
 
         dcmHandler.removeAssociationListener(dicomFindScp);
         dcmHandler.removeAssociationListener(moveScp);
     }
 
     private static final ExtNegotiator ECHO_EXT_NEG = new ExtNegotiator() {
         public byte[] negotiate(byte[] offered) {
             return offered;
         }
     };
 
     protected void enablePresContexts(AcceptorPolicy policy) {
         putPresContexts(policy, valuesToStringArray(privateCuidMap),
                 valuesToStringArray(privateTSuidMap));
         putPresContexts(policy, valuesToStringArray(standardCuidMap),
                 valuesToStringArray(tsuidMap));
     }
 
     protected void disablePresContexts(AcceptorPolicy policy) {
         putPresContexts(policy, valuesToStringArray(privateCuidMap), null);
         putPresContexts(policy, valuesToStringArray(standardCuidMap), null);
     }
 
     protected void putPresContexts(AcceptorPolicy policy, String[] cuids,
             String[] tsuids) {
         super.putPresContexts(policy, cuids, tsuids);
         ExtNegotiator neg = tsuids != null ? ECHO_EXT_NEG : null;
         for (int i = 0; i < cuids.length; i++) {
             policy.putExtNegPolicy(cuids[i], neg);
         }
     }
 
     public AEDTO queryAEData(String aet, InetAddress address)
             throws DcmServiceException, UnknownAETException {
         // String host = address != null ? address.getCanonicalHostName() :
         // null;
         try {
             Object o = server.invoke(aeServiceName, "getAE", new Object[] {
                     aet, address }, new String[] { String.class.getName(),
                     InetAddress.class.getName() });
             if (o == null)
                 throw new UnknownAETException("Unkown AET: " + aet);
             return (AEDTO) o;
         } catch (JMException e) {
             log.error("Failed to query AEData", e);
             throw new DcmServiceException(Status.UnableToProcess, e);
         }
     }
    
     List queryCorrespondingPIDs(String pid, String issuer)
     throws DcmServiceException {
         try {
             return (List) server.invoke(this.pixQueryServiceName,
                     "queryCorrespondingPIDs",
                     new Object[] { pid, 
                             issuer != null ? issuer : pixQueryDefIssuer,
                             null },
                     new String[] { String.class.getName(),
                             String.class.getName(),
                             String[].class.getName() });
         } catch (JMException e) {
             log.error("Failed to perform PIX Query", e);
             throw new DcmServiceException(Status.UnableToProcess, e);
         }
     }
     
     boolean isPixQueryLocal() throws DcmServiceException {
         try {
             return ((Boolean) server.getAttribute(this.pixQueryServiceName,
                     "PIXManagerLocal")).booleanValue();
         } catch (JMException e) {
             log.error("Failed to access PIX Service", e);
             throw new DcmServiceException(Status.UnableToProcess, e);
         }        
     }
     
     public boolean isLocalRetrieveAET(String aet) {
         for (int i = 0; i < calledAETs.length; i++) {
             if (aet.equals(calledAETs[i]))
                 return true;
         }
         return false;
     }
 
     public final int getBufferSize() {
         return bufferSize;
     }
 
     public final void setBufferSize(int bufferSize) {
         this.bufferSize = bufferSize;
     }
 
     boolean isWithoutPixelData(String moveDest) {
         return sendNoPixelDataToAETs != null
                 && Arrays.asList(sendNoPixelDataToAETs).contains(moveDest);
     }
 
     boolean isOfferNoPixelData(String moveDest) {
         return offerNoPixelDataToAETs == null
                 || Arrays.asList(offerNoPixelDataToAETs).contains(moveDest);
     }
 
     boolean isOfferNoPixelDataDeflate(String moveDest) {
         return offerNoPixelDataDeflateToAETs == null
                 || Arrays.asList(offerNoPixelDataDeflateToAETs).contains(moveDest);
     }
 
     boolean isSendWithDefaultTransferSyntax(String moveDest) {
         return sendWithDefaultTransferSyntaxToAETs == null
                 || Arrays.asList(sendWithDefaultTransferSyntaxToAETs)
                         .contains(moveDest);
     }
 
     boolean isIgnorableSOPClass(String cuid, String moveDest) {
         return ignorableSOPClasses.containsValue(cuid)
                 || ignoreUnsupportedSOPClassFailuresByAETs != null
                 && Arrays.asList(ignoreUnsupportedSOPClassFailuresByAETs)
                         .contains(moveDest);
     }
 
     String getStgCmtAET(String moveDest) {
         return (String) requestStgCmtFromAETs.get(moveDest);
     }
 
     void logInstancesSent(RemoteNode node, InstancesAction action) {
         if (auditLogger.isAuditLogIHEYr4()) {
             try {
                 server.invoke(auditLogger.getAuditLoggerName(), "logInstancesSent", new Object[] {
                         node, action }, new String[] { RemoteNode.class.getName(),
                         InstancesAction.class.getName() });
             } catch (Exception e) {
                 log.warn("Audit Log failed:", e);
             }
         }
     }
 
     protected void logInstancesSent(Association moveOrGetAs,
             Association storeAs, ArrayList fileInfos) {
         if (auditLogger.isAuditLogIHEYr4()) {
             return;
         }
         try {
             InstanceSorter sorter = new InstanceSorter();
             FileInfo fileInfo = null;
             for (Iterator iter = fileInfos.iterator(); iter.hasNext();) {
                 fileInfo = (FileInfo) iter.next();
                 sorter.addInstance(fileInfo.studyIUID, fileInfo.sopCUID,
                         fileInfo.sopIUID, null);
             }
             String destAET = storeAs.isRequestor() ? storeAs.getCalledAET()
                                                    : storeAs.getCallingAET();
             String destHost = AuditMessage.hostNameOf(
                     storeAs.getSocket().getInetAddress());
             String origAET = moveOrGetAs.getCallingAET();
             boolean dstIsRequestor = origAET.equals(destAET);
             boolean srcIsRequestor = !dstIsRequestor 
                     && Arrays.asList(calledAETs).contains(origAET);
             InstancesTransferredMessage msg = 
                     new InstancesTransferredMessage(
                             InstancesTransferredMessage.EXECUTE);
             msg.addSourceProcess(AuditMessage.getProcessID(), 
                     calledAETs, AuditMessage.getProcessName(), 
                     AuditMessage.getLocalHostName(), srcIsRequestor);
             msg.addDestinationProcess(destHost, new String[] { destAET }, null, 
                     destHost, dstIsRequestor);
             if (!dstIsRequestor && !srcIsRequestor) {
                 String origHost = AuditMessage.hostNameOf(
                         moveOrGetAs.getSocket().getInetAddress());
                 msg.addOtherParticipantProcess(origHost,
                         new String[] { origAET }, null, origHost, true);
             }
             msg.addPatient(fileInfo.patID, formatPN(fileInfo.patName));
             for (String suid : sorter.getSUIDs()) {
                 ParticipantObjectDescription desc = 
                         new ParticipantObjectDescription();
                 for (String cuid : sorter.getCUIDs(suid)) {
                     ParticipantObjectDescription.SOPClass sopClass =
                             new ParticipantObjectDescription.SOPClass(cuid);
                     sopClass.setNumberOfInstances(
                             sorter.countInstances(suid, cuid));
                     desc.addSOPClass(sopClass);
                 }
                 msg.addStudy(suid, desc);
             }
             msg.validate();
             Logger.getLogger("auditlog").info(msg);
         } catch (Exception e) {
             log.warn("Audit Log failed:", e);
         }
     }
     
     Socket createSocket(String moveCalledAET, AEDTO destAE) throws Exception {
         return tlsConfig.createSocket(aeMgr().findByAET(moveCalledAET), destAE);
     }
 
     public void queueStgCmtOrder(String calling, String called,
             Dataset actionInfo) {
         try {
             server.invoke(stgCmtScuScpName, "queueStgCmtOrder", new Object[] {
                     calling, called, actionInfo, Boolean.FALSE }, new String[] {
                     String.class.getName(), String.class.getName(),
                     Dataset.class.getName(), boolean.class.getName() });
         } catch (JMException e) {
             log.error("Failed to queue Storage Commitment Request", e);
         }
     }
 
     File retrieveFileFromTAR(String fsID, String fileID) throws Exception {
         try {
             return (File) server.invoke(tarRetrieverName, "retrieveFileFromTAR",
                     new Object[] { fsID, fileID }, new String[] {
                             String.class.getName(), String.class.getName() });
         } catch (InstanceNotFoundException e) {
             throw new ConfigurationException(e.getMessage(), e);
         } catch (MBeanException e) {
             throw e.getTargetException();
         } catch (ReflectionException e) {
             throw new ConfigurationException(e.getMessage(), e);
         }
     }
 
     private FileSystemMgt2 getFileSystemMgt() throws Exception {
         return ((FileSystemMgt2Home) EJBHomeFactory.getFactory().lookup(
                 FileSystemMgt2Home.class, FileSystemMgt2Home.JNDI_NAME)).create();
     }
 
     void updateStudyAccessTime(Set<StudyInstanceUIDAndDirPath> studyInfos) {
         if (!recordStudyAccessTime)
             return;
 
         FileSystemMgt2 fsMgt;
         try {
             fsMgt = getFileSystemMgt();
         } catch (Exception e) {
             log.fatal("Failed to access FileSystemMgt EJB");
             return;
         }
         try {
             for (Iterator<StudyInstanceUIDAndDirPath> it = studyInfos.iterator(); it.hasNext();) {
                 StudyInstanceUIDAndDirPath studyInfo = it.next();
                 try {
                     fsMgt.touchStudyOnFileSystem(studyInfo.studyIUID, studyInfo.dirpath);
                 } catch (Exception e) {
                     log.warn("Failed to update access time for study "
                             + studyInfo, e);
                 }
             }
         } finally {
             try {
                 fsMgt.remove();
             } catch (Exception ignore) {
             }
         }
     }
 
     Dataset getVMFConfig(String cuid) throws DcmServiceException {
         if (UIDs.MRImageStorage.equals(cuid))
             return getVirtualEnhancedMRConfig();
         if (UIDs.CTImageStorage.equals(cuid))
             return getVirtualEnhancedCTConfig();
         throw new DcmServiceException(0xC001,
                 "Series contains instance(s) of different SOP Classes than MR or CT - "
                         + cuid);
     }
 
     Dataset getVirtualEnhancedMRConfig() {
         if (virtualEnhancedMRConfig == null)
             virtualEnhancedMRConfig = loadVMFConfig(virtualEnhancedMRConfigFile);
         return virtualEnhancedMRConfig;
     }
 
     Dataset getVirtualEnhancedCTConfig() {
         if (virtualEnhancedCTConfig == null)
             virtualEnhancedCTConfig = loadVMFConfig(virtualEnhancedCTConfigFile);
         return virtualEnhancedCTConfig;
     }
 
     private Dataset loadVMFConfig(File file) throws ConfigurationException {
         Dataset ds = DcmObjectFactory.getInstance().newDataset();
         try {
             SAXParserFactory f = SAXParserFactory.newInstance();
             SAXParser p = f.newSAXParser();
             p.parse(FileUtils.resolve(file), ds.getSAXHandler2(null));
         } catch (Exception e) {
             throw new ConfigurationException(
                     "Failed to load VMF Configuration from " + file);
         }
         return ds;
     }
 
     void scheduleSendPendingRsp(TimerTask sendPendingRsp) {
         if (sendPendingRetrieveRSP) {
             pendingRspTimer.schedule(sendPendingRsp, 0, pendingRetrieveRSPInterval);
         }
      }
 
     Dataset makeRetrieveRspIdentifier(Collection<String> failedIUIDs) {
         if (failedIUIDs.isEmpty() )
             return null;
         Dataset ds = DcmObjectFactory.getInstance().newDataset();
         String[] a = failedIUIDs.toArray(new String[failedIUIDs.size()]);
         ds.putUI(Tags.FailedSOPInstanceUIDList, a);
         // check if 64k limit for UI attribute is reached
         if (ds.get(Tags.FailedSOPInstanceUIDList).length() < 0x10000)
             return ds;
         log.warn("Failed SOP InstanceUID List exceeds 64KB limit - send empty attribute instead");
         ds.putUI(Tags.FailedSOPInstanceUIDList);
         return ds;
     }
 
     Dimse makeCStoreRQ(ActiveAssociation activeAssoc, FileInfo info,
             int priority, String moveOriginatorAET, int moveRqMsgID,
             PerfMonDelegate perfMon) throws Exception {
         Association assoc = activeAssoc.getAssociation();
         String dest = assoc.isRequestor() ? assoc.getCalledAET() 
                 : assoc.getCallingAET();
         PresContext presCtx = selectAcceptedPresContext(assoc, info);
         if (presCtx == null)
             throw new NoPresContextException(
                     "No Presentation Context for "
                         + uidDict.toString(info.sopCUID)
                         + (assoc.isRequestor() ? " accepted by "
                                                : " offered by ")
                         + dest);
         Command storeRqCmd = DcmObjectFactory.getInstance().newCommand();
         storeRqCmd.initCStoreRQ(assoc.nextMsgID(), info.sopCUID, info.sopIUID,
                 priority);
         if (moveOriginatorAET != null) {
             storeRqCmd.putUS(Tags.MoveOriginatorMessageID, moveRqMsgID);
             storeRqCmd.putAE(Tags.MoveOriginatorAET, moveOriginatorAET);
         }
         File f = getFile(info);
         Dataset mergeAttrs = DatasetUtils.fromByteArray(info.patAttrs,
                 DatasetUtils.fromByteArray(info.studyAttrs, DatasetUtils
                         .fromByteArray(info.seriesAttrs, DatasetUtils
                                 .fromByteArray(info.instAttrs))));
         coerceOutboundCStoreRQ(mergeAttrs);
         byte[] buf = (byte[]) assoc.getProperty(SEND_BUFFER);
         if (buf == null) {
             buf = new byte[bufferSize];
             assoc.putProperty(SEND_BUFFER, buf);
         }
        
        //Modified by YangLin@cn-arg.com on 01.26.2010 for DCMQR command with -cmove
        //I don't think it is a thread safe way to use a cached buffer like above
        //Acquire DataSource instance according to Dicom image storage way
//      FileDataSource ds = new FileDataSource(f, mergeAttrs, buf);
        FileDataSource ds = (FileDataSource)ScpFactoryUtil.getScpFactory().getDataSource(
                                            f, mergeAttrs, buf.length);
        
         ds.setWithoutPixeldata(isWithoutPixelData(dest));
         Dimse rq = AssociationFactory.getInstance().newDimse(
                 presCtx.pcid(), storeRqCmd, ds);
         if (perfMon != null) {
             perfMon.setProperty(activeAssoc, rq, PerfPropertyEnum.DICOM_FILE, f);
         }
         return rq;
     }
     /**
      * Stub for manipulation of the dataset prior to issuing 
      * C-STORE rq
      * @param ds
      *              the current dataset
      */
     protected void coerceOutboundCStoreRQ(Dataset ds){}
     
     private PresContext selectAcceptedPresContext(Association a, FileInfo info) {
         String[] tsuids = { UIDs.NoPixelDataDeflate, UIDs.NoPixelData,
                 info.tsUID, UIDs.ExplicitVRLittleEndian,
                 UIDs.ImplicitVRLittleEndian
         };
         PresContext presCtx = null;
         for (int i = 0; presCtx == null && i < tsuids.length; i++) {
             presCtx = a.getAcceptedPresContext(info.sopCUID, tsuids[i]);
         }
         return presCtx;
     }
 
     protected File getFile(FileInfo info) throws Exception {
         return getFile(info.basedir, info.fileID);
     }
 
     protected File getFile(FileDTO dto) throws Exception {
         return getFile(dto.getDirectoryPath(), dto.getFilePath());
     }
 
     protected File getFile(String fsID, String fileID) throws Exception {
         return fsID.startsWith("tar:") ? retrieveFileFromTAR(fsID, fileID)
                         : FileUtils.toFile(fsID, fileID);
     }
 
     public Object locateInstance(String iuid) throws Exception {
         FileDTO[] fileDTOs = null;
         String aet = null;
         FileSystemMgt2 fsMgt = getFileSystemMgt();
         try {
             fileDTOs = fsMgt.getFilesOfInstance(iuid);
             if (fileDTOs.length == 0) {
                 aet = fsMgt.getExternalRetrieveAET(iuid);
             } else {
                 FileDTO dto;
                 for (int i = 0; i < fileDTOs.length; ++i) {
                     dto = fileDTOs[i];
                     if (isLocalRetrieveAET(dto.getRetrieveAET()))
                         return getFile(dto);
                 }
                 aet = fileDTOs[0].getRetrieveAET();
             }
         } catch (FinderException ignore) {}
         if ( aet == null ) return null;
         AEDTO aeData = aeMgr().findByAET(aet);
         return aeData;
     }
 
     public DataSource getDatasourceOfInstance(String iuid) throws Exception {
         Dataset dsQ = DcmObjectFactory.getInstance().newDataset();
         dsQ.putUI(Tags.SOPInstanceUID, iuid);
         dsQ.putCS(Tags.QueryRetrieveLevel, "IMAGE");
         RetrieveCmd retrieveCmd = RetrieveCmd.create(dsQ);
         FileInfo infoList[][] = retrieveCmd.getFileInfos();
         if (infoList.length == 0)
             return null;
         FileInfo[] fileInfos = infoList[0];
         for (int i = 0; i < fileInfos.length; ++i) {
             final FileInfo info = fileInfos[i];
             if (isLocalRetrieveAET(info.fileRetrieveAET)) {
                 File f = getFile(info);
                 Dataset mergeAttrs = DatasetUtils.fromByteArray(
                         info.patAttrs,
                         DatasetUtils .fromByteArray(
                                 info.studyAttrs,
                                 DatasetUtils .fromByteArray(
                                         info.seriesAttrs,
                                         DatasetUtils .fromByteArray(
                                                 info.instAttrs))));
                 
                 //Modified by YangLin@cn-arg.com on 03.03.2009 
                 //Acquire DataSource instance according to Dicom image storage way
 //              return new FileDataSource(f, mergeAttrs, new byte[bufferSize]);
                 return ScpFactoryUtil.getScpFactory().getDataSource(
                 		                  f, mergeAttrs, bufferSize);
                 
             }
         }
         return null;
     }
 
     void prefetchTars(Collection<List<FileInfo>> localFiles) throws Exception {
         HashSet<String> tarPaths = null;
         for (List<FileInfo> list : localFiles) {
             FileInfo fileInfo = list.get(0);
             String fsID = fileInfo.basedir;
             if (!fsID.startsWith("tar:")) {
                 continue;
             }
             String fileID = fileInfo.fileID;
             int tarEnd = fileID.indexOf('!');
             if (tarEnd == -1) {
                 // invalid fileID will be handled by retrieveLocal()
                 continue;
             }
             String tarPath = fileID.substring(0, tarEnd);
             if (tarPaths == null) {
                 tarPaths = new HashSet<String>(8);
             }
             if (tarPaths.add(tarPath)) {
                 try {
                     retrieveFileFromTAR(fsID, fileID);
                 } catch (Exception e) {
                     throw new Exception("Failed to retrieve TAR " + tarPath
                             + " from file system " + fsID, e);
                 }
              }
         }
     }
 }
