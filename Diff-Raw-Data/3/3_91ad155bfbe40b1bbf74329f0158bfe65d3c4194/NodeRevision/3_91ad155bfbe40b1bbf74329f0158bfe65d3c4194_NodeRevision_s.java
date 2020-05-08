 package org.biosharing.model;
 
 import org.biosharing.dao.BioSharingDAO;
 
 /**
  * Created by the ISA team
  *
  * @author Eamonn Maguire (eamonnmag@gmail.com)
  *         <p/>
  *         Date: 08/08/2012
  *         Time: 17:19
  */
 public class NodeRevision extends AbstractDBTable {
 
     public String toString() {
         return getVID();
     }
 
     public String getTableName() {
         return BioSharingDAO.REVISIONS_TABLE;
     }
 
     private String getVID() {
         return getValueForField(NodeRevisionFields.VID).toString();
     }
 
     public void initialiseNodeRevisionForStandard(int nid, Standard standard) {
 
         addFieldAndValue(NodeRevisionFields.NID, nid);
         addFieldAndValue(NodeRevisionFields.VID, nid);
         addFieldAndValue(NodeRevisionFields.TITLE, standard.getStandardTitle());
     }
 }
