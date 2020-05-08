 package com.sirolf2009.syringe.client.renderers;
 
 import static org.lwjgl.opengl.GL11.glDeleteLists;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.URISyntaxException;
 
 import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

 import com.sirolf2009.syringe.client.models.Model3D;
 import com.sirolf2009.syringe.parsers.parserOBJ;
 
 public class ModelRenderSimple implements IModelRenderer {
 
 	public int modelDisplayList = 0;
 	public Model3D model = null;
 	public String modelLocation = "";
 
 	public ModelRenderSimple(String modelLocation) {
 		this.modelLocation = modelLocation;
 		loadModel();
 	}
 
 	@Override
 	public void loadModel() {
 		try {
 			model = parserOBJ.loadModel(new File(getClass().getClassLoader().getResource(modelLocation).toURI()));
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 			Display.destroy();
 			System.exit(1);
 		} catch (IOException e) {
 			System.err.println("Something went wrong with loading "+modelLocation);
 			e.printStackTrace();
 			Display.destroy();
 			System.exit(1);
 		} catch (URISyntaxException e) {
 			System.err.println("Something went wrong with loading "+modelLocation);
 			e.printStackTrace();
 			Display.destroy();
 			System.exit(1);
 		}
 	}
 
 	@Override
 	public void renderModel() {
 		model.opengldraw();
 	}
 
 	@Override
 	public void disposeModel() {
 		glDeleteLists(modelDisplayList, 1);
 	}
 
 	@Override
 	public Model3D getModel() {
 		return model;
 	}
 	
 	public boolean intersects(ModelRenderSimple other) {
 		if (getModel().bottompoint < other.getModel().bottompoint || 
 				getModel().toppoint < other.getModel().toppoint || 
 				getModel().leftpoint > other.getModel().leftpoint || 
 				getModel().rightpoint > other.getModel().rightpoint ||
 				getModel().nearpoint > other.getModel().nearpoint || 
 				getModel().farpoint > other.getModel().farpoint) {
 			return false;
 		}
 		return true;
 	}
 
 }
