 package forscene.core.util;
 
 import forscene.core.entities.AbstractSceneObject;
 import forscene.core.entities.AbstractSceneObjectGroup;
 
 import playn.core.GroupLayer;
 import playn.core.Layer;
 
 public class ShapeUtil {
 	
 	public static BasicShapeInfo calculateShapeInfoGroupLayer(Layer layer)
 	{
 		BasicShapeInfo tmp = new BasicShapeInfo();
 		BasicShapeInfo tmp1 = new BasicShapeInfo();
 		Layer.HasSize t;
 		tmp.setMaxX(0);
 		tmp.setMaxY(0);
 		tmp.setMinY(0);
 		
 		if ( layer instanceof GroupLayer)
 		{
 			GroupLayer tmpLayer = (GroupLayer)layer;
 			for( int i = 0 ; i< tmpLayer.size(); i++)
 			{
 				if (tmpLayer.get(i) instanceof GroupLayer)
 				{
 					tmp1=calculateShapeInfoGroupLayer((GroupLayer)tmpLayer.get(i));
 					if(tmp.getMaxX() < tmp1.getMaxX()) tmp.setMaxX(tmp1.getMaxX());				
 					if(tmp.getMaxY() < tmp1.getMaxY()) tmp.setMaxY(tmp1.getMaxY());
 					if(tmp.getMinX() > tmp1.getMinX()) tmp.setMinX(tmp1.getMinX());
 					if(tmp.getMinY() > tmp1.getMinY()) tmp.setMinY(tmp1.getMinY());
 				}
				if (tmpLayer instanceof Layer.HasSize)
				{			
					t=(Layer.HasSize)tmpLayer.get(i);
					if (tmp.getMaxX() < (t.scaledWidth()+t.originX()) )
					tmp.setMaxX(t.scaledWidth()+t.originX());
				
					if (tmp.getMaxY() < (t.scaledHeight()+t.originY()) )
						tmp.setMaxY(t.scaledHeight()+t.originY());
				
					if (tmp.getMinX() > (t.scaledWidth()+t.originX()) )
						tmp.setMinX(t.scaledWidth()+t.originX());
				
					if (tmp.getMinY() > (t.scaledHeight()+t.originY()) )
						tmp.setMinY(t.scaledHeight()+t.originY());							
				}						
 			}
 		}
 		return tmp;
 	}
 	
 	public static BasicShapeInfo calculateShapeInfoSceneObject(AbstractSceneObject aso)
 	{
 		BasicShapeInfo tmp = new BasicShapeInfo();
 		BasicShapeInfo tmp1 = new BasicShapeInfo();		
 		tmp.setMaxX(0);
 		tmp.setMaxY(0);
 		tmp.setMinY(0);
 		if (aso instanceof AbstractSceneObjectGroup) //TODO: FIXIT ! WRONG!!!!!! 
 		{
 			for( int i = 0 ; i< ((AbstractSceneObjectGroup)aso).getSize(); i++)
 			{
 				tmp1=calculateShapeInfoSceneObject(((AbstractSceneObjectGroup) aso).getSceneObject(i));			
 				if(tmp.getMaxX() < tmp1.getMaxX()) tmp.setMaxX(tmp1.getMaxX());				
 				if(tmp.getMaxY() < tmp1.getMaxY()) tmp.setMaxY(tmp1.getMaxY());
 				if(tmp.getMinX() > tmp1.getMinX()) tmp.setMinX(tmp1.getMinX());
 				if(tmp.getMinY() > tmp1.getMinY()) tmp.setMinY(tmp1.getMinY());
 			}
 		}
 		tmp=calculateShapeInfoGroupLayer(aso.getRoot());
 		return tmp;
 	}
 	
 }
