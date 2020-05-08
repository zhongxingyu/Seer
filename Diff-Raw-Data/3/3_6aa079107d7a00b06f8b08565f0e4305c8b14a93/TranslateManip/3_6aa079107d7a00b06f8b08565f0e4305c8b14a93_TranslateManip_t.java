 package cs4620.manip;
 
 import javax.media.opengl.GL2;
 import javax.vecmath.Matrix3f;
 import javax.vecmath.Matrix4f;
 import javax.vecmath.Vector2f;
 import javax.vecmath.Vector3f;
 
 import cs4620.scene.SceneNode;
 import cs4620.scene.SceneProgram;
 import cs4620.shape.Cube;
 import cs4620.shape.TriangleMesh;
 import cs4620.test.ManipTestHelpers;
 import cs4620.framework.Camera;
 import cs4620.framework.Transforms;
 import cs4620.manip.ManipUtils;
 import cs4620.material.Material;
 import cs4620.material.PhongMaterial;
 
 public class TranslateManip extends Manip
 {
 	Cube cube = null;
 	ArrowHeadMesh arrow = null;
 	PhongMaterial xMaterial;
 	PhongMaterial yMaterial;
 	PhongMaterial zMaterial;
 	PhongMaterial centerMaterial;
 	boolean resourcesInitialized = false;
 	
 	private void initResourcesGL(GL2 gl)
 	{
 		if (!resourcesInitialized)
 		{
 			cube = new Cube(gl);
 			cube.buildMesh(gl, 1.0f);
 			arrow = new ArrowHeadMesh(gl);
 			arrow.buildMesh(gl, 1.0f);
 			
 			xMaterial = new PhongMaterial();
 			xMaterial.setAmbient(0.0f, 0.0f, 0.0f);
 			xMaterial.setDiffuse(0.8f, 0.0f, 0.0f);
 			xMaterial.setSpecular(0.0f, 0.0f, 0.0f);
 			
 			yMaterial = new PhongMaterial();
 			yMaterial.setAmbient(0.0f, 0.0f, 0.0f);
 			yMaterial.setDiffuse(0.0f, 0.8f, 0.0f);
 			yMaterial.setSpecular(0.0f, 0.0f, 0.0f);
 			
 			zMaterial = new PhongMaterial();
 			zMaterial.setAmbient(0.0f, 0.0f, 0.0f);
 			zMaterial.setDiffuse(0.0f, 0.0f, 0.8f);
 			zMaterial.setSpecular(0.0f, 0.0f, 0.0f);
 			
 			centerMaterial = new PhongMaterial();
 			centerMaterial.setAmbient(0.0f, 0.0f, 0.0f);
 			centerMaterial.setDiffuse(0.8f, 0.8f, 0.0f);
 			centerMaterial.setSpecular(0.0f, 0.0f, 0.0f);
 			resourcesInitialized = true;
 		}
 	}
 	
 	@Override
 	public void dragged(Vector2f mousePosition, Vector2f mouseDelta)
 	{
 		// TODO (Manipulators P1): Implement this manipulator.
 		
 		Vector2f initPoint = new Vector2f(mousePosition);
 		initPoint.sub(mouseDelta);
 		Vector2f finalPoint = new Vector2f(mousePosition);
 		
 		Vector3f initNDCpoint = new Vector3f();
 		Vector3f initNDCvect = new Vector3f();
 		Vector3f finalNDCpoint = new Vector3f();
 		Vector3f finalNDCvect = new Vector3f();
 		
 		camera.getRayNDC(initPoint, initNDCpoint, initNDCvect);
 		camera.getRayNDC(finalPoint, finalNDCpoint, finalNDCvect);
 		
 		SceneNode parent = sceneNode.getSceneNodeParent();
 		Matrix4f parTrans = new Matrix4f(parent.toWorld());
 		
 		Vector3f transOrig = new Vector3f(0, 0, 0);
 		parTrans.transform(transOrig);
 		
 		if (this.axisMode == PICK_X){
 			Vector3f transX = new Vector3f(eX);
 			parTrans.transform(transX);
 			
 			float t0 = ManipUtils.timeClosestToRay(transOrig, transX, initNDCpoint, initNDCvect);
 			float t1 = ManipUtils.timeClosestToRay(transOrig, transX, finalNDCpoint, finalNDCvect);
 			
 			float change = t1 - t0;
 			sceneNode.translation.set(sceneNode.translation.x+change,
 					sceneNode.translation.y,
 					sceneNode.translation.z);
 		}
 		if (this.axisMode == PICK_Y){
 			Vector3f transY = new Vector3f(eY);
 			parTrans.transform(transY);
 			
 			float t0 = ManipUtils.timeClosestToRay(transOrig, transY, initNDCpoint, initNDCvect);
 			float t1 = ManipUtils.timeClosestToRay(transOrig, transY, finalNDCpoint, finalNDCvect);
 			
 			float change = t1 - t0;
 			sceneNode.translation.set(sceneNode.translation.x,
 					sceneNode.translation.y+change,
 					sceneNode.translation.z);
 		}
 		if (this.axisMode == PICK_Z){
 			Vector3f transZ = new Vector3f(eZ);
 			parTrans.transform(transZ);
 			
 			float t0 = ManipUtils.timeClosestToRay(transOrig, transZ, initNDCpoint, initNDCvect);
 			float t1 = ManipUtils.timeClosestToRay(transOrig, transZ, finalNDCpoint, finalNDCvect);
 			
 			float change = t1 - t0;
 			sceneNode.translation.set(sceneNode.translation.x,
 					sceneNode.translation.y,
 					sceneNode.translation.z+change);
 		}
 		if (this.axisMode == PICK_CENTER){
 			Vector3f planeN = camera.getViewDir();
 			Vector3f planeP = transOrig;
 			
 			float Pold;
 			float Pnew;
 			
 			Pold = ManipUtils.intersectRayPlane(initNDCpoint, initNDCvect, planeP, planeN);
 			Pnew = ManipUtils.intersectRayPlane(finalNDCpoint, finalNDCvect, planeP, planeN);
 			
 			//p + tv
 			Vector3f oldP= initNDCpoint;
 			Vector3f newP= finalNDCpoint;
 			initNDCvect.scale(Pold);
 			finalNDCvect.scale(Pnew);
 			oldP.add(initNDCvect);
 			newP.add(finalNDCvect);
 			
 			Vector3f change = newP;
 			change.sub(oldP);
 			
			parTrans.invert();
			parTrans.transform(change);
			
 			sceneNode.translation.set(sceneNode.translation.x+change.x,
 					sceneNode.translation.y+change.y,
 					sceneNode.translation.z+change.z);
 		}
 	}
 
 	@Override
 	public void glRender(GL2 gl, SceneProgram program, Matrix4f modelView, double scale)
 	{
 		initResourcesGL(gl);
 		
 		SceneNode parent = sceneNode.getSceneNodeParent();
 		Matrix4f parentModelView;
 		if (parent != null)
 		{
 			parentModelView = parent.toEye(modelView);
 		}
 		else
 		{
 			parentModelView = new Matrix4f(modelView);
 		}
 		
 		// get eye-space vectors for x,y,z axes of parent
 		Vector3f eyeX = new Vector3f();
 		Vector3f eyeY = new Vector3f();
 		Vector3f eyeZ = new Vector3f();
 		Transforms.toColumns3DH(parentModelView, eyeX, eyeY, eyeZ, null);
 		
 		// get eye-space position of translated origin of sceneNode
 		Vector3f eyeOrigin = new Vector3f(sceneNode.translation);
 		ManipUtils.transformPosition(parentModelView, eyeOrigin);
 		
 		// translation to eye-space translated origin of sceneNode
 		Matrix4f translateOrigin = Transforms.translate3DH(eyeOrigin);
 		Matrix4f scaleMatrix = Transforms.scale3DH((float) scale);
 		
 		Matrix4f nextModelView = new Matrix4f();
 		
 		// x axis
 		nextModelView.set(translateOrigin);
 		nextModelView.mul(ManipUtils.rotateZTo(eyeX));
 		nextModelView.mul(scaleMatrix);
 		setIdIfPicking(gl, program, Manip.PICK_X);
 		glRenderArrow(gl, program, nextModelView, 0);
 		
 		// y axis
 		nextModelView.set(translateOrigin);
 		nextModelView.mul(ManipUtils.rotateZTo(eyeY));
 		nextModelView.mul(scaleMatrix);
 		setIdIfPicking(gl, program, Manip.PICK_Y);
 		glRenderArrow(gl, program, nextModelView, 1);
 		
 		// z axis
 		nextModelView.set(translateOrigin);
 		nextModelView.mul(ManipUtils.rotateZTo(eyeZ));
 		nextModelView.mul(scaleMatrix);
 		setIdIfPicking(gl, program, Manip.PICK_Z);
 		glRenderArrow(gl, program, nextModelView, 2);
 		
 		float boxRadius = 0.1f;
 		// center cube
 		nextModelView.set(translateOrigin);
 		nextModelView.mul(scaleMatrix);
 		nextModelView.mul(Transforms.scale3DH(boxRadius));
 		program.setMaterial(gl, centerMaterial);
 		program.setModelView(gl, nextModelView);
 		setIdIfPicking(gl, program, Manip.PICK_CENTER);
 		gl.glDisable(GL2.GL_DEPTH_TEST);
 		cube.draw(gl);  // cube should draw over other parts of manipulator
 		gl.glEnable(GL2.GL_DEPTH_TEST);
 	}
 
 	public void glRenderArrow(GL2 gl, SceneProgram program, Matrix4f modelView, int axis) {
 		Material axisMaterial = zMaterial;
 		if (axis == 0) // x
 		{
 			axisMaterial = xMaterial;
 		}
 		else if (axis == 1) // y
 		{
 			axisMaterial = yMaterial;
 		}
 		
 		float radiusTail = 0.075f;
 		float radiusHead = 0.15f;
 		float lengthTail = 1.7f;
 		float lengthHead = 0.3f;
 		
 		program.setMaterial(gl, axisMaterial);
 		
 		// tail
 		Matrix4f nextModelView = new Matrix4f(modelView);
 		nextModelView.mul(Transforms.scale3DH(radiusTail, radiusTail, -lengthTail / 2.0f));
 		nextModelView.mul(Transforms.translate3DH(0.0f, 0.0f, -1.0f));
 		program.setModelView(gl, nextModelView);
 		arrow.draw(gl);
 		
 		// tail
 		nextModelView.set(modelView);
 		nextModelView.mul(Transforms.translate3DH(0.0f, 0.0f, lengthTail));
 		nextModelView.mul(Transforms.scale3DH(radiusHead, radiusHead, lengthHead / 2.0f));
 		nextModelView.mul(Transforms.translate3DH(0.0f, 0.0f, 1.0f));
 		program.setModelView(gl, nextModelView);
 		arrow.draw(gl);
 		
 	}
 }
 
 class ArrowHeadMesh extends TriangleMesh
 {
 	
 	public static final int CIRCLE_DIVS = 32;
 
 	public ArrowHeadMesh(GL2 gl) {
 		super(gl);
 	}
 	
 	float cos(float ang)
 	{
 		return (float) Math.cos(ang);
 	}
 	
 	float sin(float ang)
 	{
 		return (float) Math.sin(ang);
 	}
 
 	@Override
 	public void buildMesh(GL2 gl, float tolerance) {
 		// build the arrowhead!
 		float [] vertices = new float [3 * CIRCLE_DIVS * 2];
 		float [] normals = new float [3 * CIRCLE_DIVS * 2];
 		
 		int [] triangles = new int [3 * CIRCLE_DIVS];
 		
 		float pi = (float) Math.PI;
 		float xyCoeff = 2.0f / (float) Math.sqrt(5.0f);
 		float zCoeff = 1.0f / (float) Math.sqrt(5.0f);
 		// form vertices and normals
 		for(int i = 0; i < CIRCLE_DIVS; i++)
 		{
 			float ang = i * 2.0f * pi / CIRCLE_DIVS;
 			float angBetween = (2 * i + 1) * 2.0f * pi / (2 * CIRCLE_DIVS);
 			
 			vertices[6*i + 0] = cos(ang);
 			vertices[6*i + 1] = sin(ang);
 			vertices[6*i + 2] = -1.0f;
 			normals[6*i + 0] = cos(ang) * xyCoeff;
 			normals[6*i + 1] = sin(ang) * xyCoeff;
 			normals[6*i + 2] = zCoeff;
 			
 			vertices[6*i + 3] = 0.0f;
 			vertices[6*i + 4] = 0.0f;
 			vertices[6*i + 5] = 1.0f;
 			normals[6*i + 3] = cos(angBetween) * xyCoeff;
 			normals[6*i + 4] = sin(angBetween) * xyCoeff;
 			normals[6*i + 5] = zCoeff;
 		}
 		
 		// assemble triangles
 		for(int i = 0; i < CIRCLE_DIVS; i++)
 		{
 			triangles[3 * i + 0] = 2*i;
 			triangles[3 * i + 1] = (2*i + 2) % (2 * CIRCLE_DIVS);
 			triangles[3 * i + 2] = 2*i+1;
 		}
 		
 		this.setVertices(gl, vertices);
 		this.setNormals(gl, normals);
 		this.setTriangleIndices(gl, triangles);
 		// we won't need wireframe here
 		this.setWireframeIndices(gl, new int [0]);
 	}
 
 	@Override
 	public Object getYamlObjectRepresentation() {
 		return null;
 	}
 	
 }
