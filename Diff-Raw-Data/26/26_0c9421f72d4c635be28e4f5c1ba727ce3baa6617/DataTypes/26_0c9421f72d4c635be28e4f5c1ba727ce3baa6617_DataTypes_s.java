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
 
 package org.amanzi.awe.views.reuse.mess_table;
 
 import org.amanzi.neo.core.enums.DriveTypes;
 import org.amanzi.neo.core.enums.NodeTypes;
 import org.amanzi.neo.core.utils.NeoUtils;
 import org.neo4j.graphdb.GraphDatabaseService;
 import org.neo4j.graphdb.Node;
 
 /**
  * TODO Purpose of 
  * <p>
  *
  * </p>
  * @author Shcharbatsevich_A
  * @since 1.0.0
  */
 public enum DataTypes {
 
     ROMES(DriveTypes.ROMES,NodeTypes.M),
     NEMO1(DriveTypes.NEMO1,NodeTypes.M),
     NEMO2(DriveTypes.NEMO2,NodeTypes.M),
     AMS(DriveTypes.AMS,NodeTypes.M),
     AMS_CALLS(DriveTypes.AMS_CALLS,NodeTypes.CALL),
     TEMS(DriveTypes.TEMS,NodeTypes.M),
     MS(DriveTypes.MS,NodeTypes.M),
    OSS(null,NodeTypes.GPEH_EVENT);
     
     private DriveTypes type;
     private NodeTypes childType;
     
     private DataTypes(DriveTypes aType,NodeTypes aChildType) {
         type = aType;
         childType = aChildType;
     }
     
     /**
      * @return Returns the type.
      */
     public DriveTypes getType() {
         return type;
     }
     
     /**
      * @return Returns the childType.
      */
     public NodeTypes getChildType() {
         return childType;
     }
     
     public static DataTypes getTypeByNode(Node aNode, GraphDatabaseService service){
         DriveTypes key = NeoUtils.getDatasetType(aNode, service);
        if(key==null && NodeTypes.OSS.checkNode(aNode)){
            return OSS;
         }
         for(DataTypes curr : values()){
             if(key.equals(curr.type)){
                 return curr;
             }
         }
         return null;
     }
 }
