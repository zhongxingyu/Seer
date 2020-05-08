 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.ListIterator;
 import java.util.Vector;
 
 import javax.media.opengl.GL;
 
 
 abstract public class SceneNode {
 
 	protected float[] rot, trans, scale, color, objBounds, trueBounds, trueBoundsInWorld;
 
 	protected LinkedList<SceneNode> children;
 	protected SceneNode parent;
 	
 	GL gl;
 	
 	public SceneNode(GL gl){
 		this.gl = gl;
 		children = new LinkedList<SceneNode>();		
 		rot = new float[3];
 		trans = new float[3];
 		color = new float[4];
 		scale = new float[] {1, 1, 1};
 		objBounds = new float[6];
 		trueBounds = new float[6];
 		trueBoundsInWorld = new float[6];
 	}
 	
 	public void translate (float transX, float transY, float transZ){
 		trans[0] = transX;
 		trans[1] = transY;
 		trans[2] = transZ;		
 	}
 	
 	public void rotate (float rotX, float rotY, float rotZ){		
 		rot[0] = rotX;
 		rot[1] = rotY;
 		rot[2] = rotZ;
 	}
 	
 	public void scale (float scaleX, float scaleY, float scaleZ){
 		scale[0] = scaleX;
 		scale[1] = scaleY;
 		scale[2] = scaleZ;
 	}
 	
 	public void color (float r, float g, float b, float a){
 		color[0] = r;
 		color[1] = g;
 		color[2] = b;
 		color[3] = a;
 	}
 	
 	public float[] getRot() {
 		return rot;
 	}
 
 	public float[] getTrans() {
 		return trans;
 	}
 
 	public float[] getScale() {
 		return scale;
 	}
 	
 	public void objBounds(float xBoundMin, float yBoundMin, float zBoundMin,
 						  float xBoundMax, float yBoundMax, float zBoundMax){
 		objBounds[0] = xBoundMin;
 		objBounds[1] = yBoundMin;
 		objBounds[2] = zBoundMin;
 		objBounds[3] = xBoundMax;
 		objBounds[4] = yBoundMax;
 		objBounds[5] = zBoundMax;
 	}
 	
 	public float[] getTrueBounds(){
 		return trueBounds;
 	}
 	
 	public float[] getTrueBoundsInWorld(){		
 		return trueBoundsInWorld;
 	}
 	
 	public float[] getObjBounds(){
 		return objBounds;
 	}
 	
 	public void addChild (SceneNode child){
 		children.add(child);
 		child.setParent(this);
 	}
 	
 	public void setParent (SceneNode parent){
 		this.parent = parent;
 	}
 	
 	public SceneNode getParent(){
 		return parent;
 	}
 	
 	public void draw (boolean drawBounds){
 		gl.glPushMatrix();				
 		gl.glTranslatef(trans[0], trans[1], trans[2]);
 		gl.glScalef(scale[0], scale[1], scale[2]);
 		gl.glRotatef(rot[0], 1, 0, 0);
 		gl.glRotatef(rot[1], 0, 1, 0);
 		gl.glRotatef(rot[2], 0, 0, 1);	
 		drawMe();		
 		if (drawBounds)
 			drawBounds();
 		gl.glPopMatrix();
 	}	
 	
 	public void drawBounds(){
 		gl.glColor3f(1, 1, 1);
 		gl.glBegin(GL.GL_LINE_STRIP);					
 		gl.glVertex3f(trueBounds[0], trueBounds[1], trueBounds[2]);
 		gl.glVertex3f(trueBounds[3], trueBounds[1], trueBounds[2]);
 		gl.glVertex3f(trueBounds[3], trueBounds[4], trueBounds[2]);
 		gl.glVertex3f(trueBounds[0], trueBounds[4], trueBounds[2]);
 		gl.glVertex3f(trueBounds[0], trueBounds[1], trueBounds[2]);
 		gl.glVertex3f(trueBounds[0], trueBounds[1], trueBounds[5]);		
 		gl.glVertex3f(trueBounds[3], trueBounds[1], trueBounds[5]);		
 		gl.glVertex3f(trueBounds[3], trueBounds[4], trueBounds[5]);		
 		gl.glVertex3f(trueBounds[0], trueBounds[4], trueBounds[5]);
 		gl.glVertex3f(trueBounds[0], trueBounds[1], trueBounds[5]);
 		gl.glEnd();
 		gl.glBegin(GL.GL_LINES);
 		gl.glVertex3f(trueBounds[3], trueBounds[1], trueBounds[5]);
 		gl.glVertex3f(trueBounds[3], trueBounds[1], trueBounds[2]);		
 		gl.glVertex3f(trueBounds[3], trueBounds[4], trueBounds[5]);		
 		gl.glVertex3f(trueBounds[3], trueBounds[4], trueBounds[2]);
 		gl.glVertex3f(trueBounds[0], trueBounds[4], trueBounds[5]);
 		gl.glVertex3f(trueBounds[0], trueBounds[4], trueBounds[2]);
 		gl.glEnd();			
 	}
 
 	public void updateBounds(){		
 		
 		for (int i = 0; i < 6; ++i)
 			trueBounds[i] = objBounds[i];
 		
 		ListIterator<SceneNode> it = children.listIterator();
 		while (it.hasNext()){			
 			SceneNode child = it.next();
 			float[] scaleC = child.getScale();
 			float[] transC = child.getTrans();
 			float[] rotC = child.getRot();
 			float[] childBounds = child.getTrueBounds();
 			float[] mat = new float[16];		
 			
 			mat = Matrix.genIdent();			
 			mat = Matrix.matMult(mat, Matrix.genTrans(transC[0], transC[1], transC[2]));
 			mat = Matrix.matMult(mat, Matrix.genScale(scaleC[0], scaleC[1], scaleC[2]));
 			mat = Matrix.matMult(mat, Matrix.genRotX(rotC[0]));
 			mat = Matrix.matMult(mat, Matrix.genRotY(rotC[1]));
 			mat = Matrix.matMult(mat, Matrix.genRotZ(rotC[2]));
 			
 			float[] tmp;
 			for (int i = 0; i <= 3; i = i+3)
 				for (int j = 1; j <= 4; j = j+3)
 					for (int k = 2; k <= 5; k = k+3){
 						tmp = Matrix.mult(mat, childBounds[i], childBounds[j], childBounds[k]);
 						for (int l = 0; l < 3; ++l)
 							if (tmp[l] < trueBounds[l])
 								trueBounds[l] = tmp[l];
 						for (int l = 3; l < 6; ++l)
 							if (tmp[l-3] > trueBounds[l])
 								trueBounds[l] = tmp[l-3];
 					}
 		}					
 		
 		if (parent != null)
 			parent.updateBounds();
 		
 		updateTrueBoundsInWorld();
 	}
 	
 	protected void updateTrueBoundsInWorld(){
 		LinkedList<SceneNode> ancestors = new LinkedList<SceneNode>();
 		SceneNode parent = this;
 		ancestors.add(0, parent);
 		while (parent.getParent() != null){
 			parent = parent.getParent();
 			ancestors.add(0, parent);
 		}
 		
 		float[] matrix = Matrix.genIdent();		
 		ListIterator<SceneNode> it = ancestors.listIterator();
 		while (it.hasNext()){
 			SceneNode current = it.next();
 			
 			float vec[] = current.getTrans();
 			matrix = Matrix.matMult(matrix, Matrix.genTrans(vec[0], vec[1], vec[2]));
 			
 			vec = current.getScale();
 			matrix = Matrix.matMult(matrix, Matrix.genScale(vec[0], vec[1], vec[2]));
 			
 			vec = current.getRot();			
 			matrix = Matrix.matMult(matrix, Matrix.genRotX(vec[0]));
 			matrix = Matrix.matMult(matrix, Matrix.genRotY(vec[1]));
 			matrix = Matrix.matMult(matrix, Matrix.genRotZ(vec[2]));
 		}
 		
 		float[] tmp;
 		for (int i = 0; i < 3; ++i)
 			trueBoundsInWorld[i] = Integer.MAX_VALUE;
 		for (int i = 3; i < 6; ++i)
 			trueBoundsInWorld[i] = Integer.MIN_VALUE;
 		
 		for (int i = 0; i <= 3; i = i+3)
 			for (int j = 1; j <= 4; j = j+3)
 				for (int k = 2; k <= 5; k = k+3){
 					tmp = Matrix.mult(matrix, trueBounds[i], trueBounds[j], trueBounds[k]);
 					for (int l = 0; l < 3; ++l)
 						if (tmp[l] < trueBoundsInWorld[l])
 							trueBoundsInWorld[l] = tmp[l];
 					for (int l = 3; l < 6; ++l)
 						if (tmp[l-3] > trueBoundsInWorld[l])
 							trueBoundsInWorld[l] = tmp[l-3];
 				}
 				
 		tmp = null;
 	}
 	
 	abstract public void drawMe();
 	
 	public void drawSorted(boolean drawBounds, float[] cameraPos){
 		if (children.size() == 0){
 			draw(drawBounds);
 			return;
 		}		
 		AlphaComp acom = new AlphaComp(cameraPos);
 		
 		boolean intersect = false;
 		//TODO: Insert intersection check!
 		
 		if (intersect){
 			
 		} else {
 			gl.glPushMatrix();					
 			gl.glTranslatef(trans[0], trans[1], trans[2]);
 			gl.glScalef(scale[0], scale[1], scale[2]);
 			gl.glRotatef(rot[0], 1, 0, 0);
 			gl.glRotatef(rot[1], 0, 1, 0);
 			gl.glRotatef(rot[2], 0, 0, 1);	
 			
 			if (drawBounds)
 				drawBounds();
 			
 			Collections.sort(children, acom);
 			ListIterator<SceneNode> it = children.listIterator();			
 			while (it.hasNext())
 				it.next().drawSorted(drawBounds, cameraPos);
 			
 			gl.glPopMatrix();
 		}
 		
 	}
 }
