 package com.iver.cit.gvsig.project.documents.view.snapping;
 
 import com.iver.cit.gvsig.project.documents.view.snapping.snappers.CentralPointSnapper;
 import com.iver.cit.gvsig.project.documents.view.snapping.snappers.FinalPointSnapper;
 import com.iver.cit.gvsig.project.documents.view.snapping.snappers.IntersectionPointSnapper;
 import com.iver.cit.gvsig.project.documents.view.snapping.snappers.MediumPointSnapper;
 import com.iver.cit.gvsig.project.documents.view.snapping.snappers.NearestPointSnapper;
 import com.iver.cit.gvsig.project.documents.view.snapping.snappers.PerpendicularPointSnapper;
 import com.iver.cit.gvsig.project.documents.view.snapping.snappers.PixelSnapper;
 import com.iver.cit.gvsig.project.documents.view.snapping.snappers.QuadrantPointSnapper;
 import com.iver.cit.gvsig.project.documents.view.snapping.snappers.TangentPointSnapper;
 import com.iver.utiles.extensionPoints.ExtensionPoints;
 import com.iver.utiles.extensionPoints.ExtensionPointsSingleton;
 
 /**
  * DOCUMENT ME!
  *
  * @author Vicente Caballero Navarro
  */
 public class Snapping {
     /**
      * DOCUMENT ME!
      */
     public static void register() {
     	ExtensionPoints extensionPoints = ExtensionPointsSingleton.getInstance();
     	extensionPoints.add("Snapper","FinalPointSnapper", FinalPointSnapper.class);
     	extensionPoints.add("Snapper","NearestPointSnapper", NearestPointSnapper.class);
     	extensionPoints.add("Snapper","PixelSnapper", PixelSnapper.class);
     	extensionPoints.add("Snapper","CentralPointSnapper", CentralPointSnapper.class);
     	extensionPoints.add("Snapper","QuadrantPointSnapper", QuadrantPointSnapper.class);
//    	extensionPoints.add("Snapper","InsertPointSnapper", InsertPointSnapper.class);
     	extensionPoints.add("Snapper","IntersectionPointSnapper", IntersectionPointSnapper.class);
     	extensionPoints.add("Snapper","MediumPointSnapper", MediumPointSnapper.class);
     	extensionPoints.add("Snapper","PerpendicularPointSnapper", PerpendicularPointSnapper.class);
     	extensionPoints.add("Snapper","TangentPointSnapper", TangentPointSnapper.class);
     }
 }
