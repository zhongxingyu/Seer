 package com.ggervais.gameengine.scene.scenegraph;
 
 import com.ggervais.gameengine.geometry.primitives.*;
 import com.ggervais.gameengine.math.*;
 import com.ggervais.gameengine.physics.boundingvolumes.BoundingBox;
 import com.ggervais.gameengine.physics.boundingvolumes.BoundingSphere;
 import com.ggervais.gameengine.render.DisplaySubsystem;
 import com.ggervais.gameengine.render.SceneRenderer;
 import com.ggervais.gameengine.scene.scenegraph.renderstates.GlobalState;
 import com.ggervais.gameengine.scene.scenegraph.renderstates.GlobalStateType;
 import org.apache.log4j.Logger;
 
 import java.util.*;
 
 public class Geometry extends Spatial {
     private static final int DEFAULT_NB_VERTICES_PER_FACE = 3;
     private static final Logger log = Logger.getLogger(Geometry.class);
 
 	protected VertexBuffer vertexBuffer;
 	protected IndexBuffer indexBuffer;
 	protected TextureBuffer textureBuffer; // Per face texture coordinates.
 	protected int nbVerticesPerFace;
 	private List<Face> faces;
     protected int nbFaces;
 
     private BoundingBox modelBoundingBox;
    private boolean isBoundingBoxDirty;
    private boolean isBoundingSphereDirty;
 
   private Map<GlobalStateType, GlobalState> globalStates;
 
 	public Geometry() {
 		this(DEFAULT_NB_VERTICES_PER_FACE); // Defaults to triangles.
 	}
 
 	public Geometry(int nbVerticesPerFace) {
 		super();
         this.vertexBuffer = new VertexBuffer();
 		this.indexBuffer = new IndexBuffer();
 		this.textureBuffer = new TextureBuffer();
 		this.nbVerticesPerFace = nbVerticesPerFace;
 		this.faces = new ArrayList<Face>();
         this.isBoundingBoxDirty = true;
         this.isBoundingSphereDirty = true;
         this.globalStates = new HashMap<GlobalStateType, GlobalState>();
         this.modelBoundingBox = new BoundingBox(Point3D.zero(), Point3D.zero());
         this.nbFaces = 0;
 	}
 
    public void setBoundingBoxDirty(boolean dirty) {
        this.isBoundingBoxDirty = dirty;
    }
 
     public void addFace(Face face) {
         this.faces.add(face);
         resetNbFaces();
     }
 
     public void setNbFaces(int nbFaces) {
         this.nbFaces = MathUtils.clamp(nbFaces, 0, this.faces.size());
     }
 
     public Face getFace(int index) {
         if (index < 0 || index > this.faces.size() - 1) {
             return null;
         }
 
         return this.faces.get(index);
     }
 
     public int getNbFaces() {
         return this.nbFaces;
     }
 
     public void resetNbFaces() {
         this.nbFaces = this.faces.size();
     }
 
 	public VertexBuffer getVertexBuffer() {
 		return this.vertexBuffer;
 	}
 
 	public IndexBuffer getIndexBuffer() {
 		return this.indexBuffer;
 	}
 
 	public TextureBuffer getTextureBuffer() {
 		return this.textureBuffer;
 	}
 
 	public int getNbVerticesPerFace() {
 		return this.nbVerticesPerFace;
 	}
 
 	public float getScale() {
 		return 1.0f;
 	}
 
     private void computeBoundingSphere(Matrix4x4 transform) {
         float sumX = 0;
         float sumY = 0;
         float sumZ = 0;
 
         int nbVertices = 0;
 
         for (int i = 0; i < this.vertexBuffer.size(); i++) {
             Vertex vertex = this.vertexBuffer.getVertex(i);
             Point3D position = transform.mult(vertex.getPosition());
             sumX += position.x();
             sumY += position.y();
             sumZ += position.z();
 
             nbVertices++;
         }
 
         float averageX = sumX / nbVertices;
         float averageY = sumY / nbVertices;
         float averageZ = sumZ / nbVertices;
 
         Point3D center = new Point3D(averageX, averageY, averageZ);
 
         float radiusSquared = Float.MIN_VALUE;
         for (int i = 0; i < this.vertexBuffer.size(); i++) {
             Vertex vertex = this.vertexBuffer.getVertex(i);
             Point3D position = transform.mult(vertex.getPosition());
 
             Vector3D diff = position.sub(center);
             radiusSquared = Math.max(diff.lengthSquared(), radiusSquared);
         }
 
         // Only compute sqrt at the end.
         float radius = (float) Math.sqrt(radiusSquared);
 
         BoundingSphere sphere = new BoundingSphere(center, radius);
 
         this.boundingSphere = sphere;
     }
 
     private void computeBoundingBox(Matrix4x4 transform) {
 
 
         // TODO fix precision
 
         float minX = 999999;
         float minY = 999999;
         float minZ = 999999;
 
         float maxX = -999999;
         float maxY = -999999;
         float maxZ = -999999;
 
         /*float minX = Float.MAX_VALUE;
         float minY = Float.MAX_VALUE;
         float minZ = Float.MAX_VALUE;
 
         float maxX = Float.MIN_VALUE;
         float maxY = Float.MIN_VALUE;
         float maxZ = Float.MIN_VALUE;*/
 
         for (int i = 0; i < this.vertexBuffer.size(); i++) {
             Vertex vertex = this.vertexBuffer.getVertex(i);
             Point3D position = transform.mult(vertex.getPosition());
 
             minX = Math.min(position.x(), minX);
             minY = Math.min(position.y(), minY);
             minZ = Math.min(position.z(), minZ);
             maxX = Math.max(position.x(), maxX);
             maxY = Math.max(position.y(), maxY);
             maxZ = Math.max(position.z(), maxZ);
         }
 
         Point3D minCorner = new Point3D(minX, minY, minZ);
         Point3D maxCorner = new Point3D(maxX, maxY, maxZ);
         BoundingBox box = new BoundingBox(minCorner, maxCorner);
 
         this.modelBoundingBox = box;
     }
 
     @Override
     public void setEffect(Effect effect) {
 
         int size = this.vertexBuffer.getRealSize();
 
         effect.resetColorList();
         effect.initializeColors(size);
         for (int i = 0; i < size; i++) {
             effect.setColor(i, effect.getColor());
         }
 
         super.setEffect(effect);
     }
 
     @Override
     public void updateWorldBound() {
 
        //this.isBoundingBoxDirty = true;
         if (this.isBoundingBoxDirty) {
            computeBoundingBox(new Matrix4x4());
             this.isBoundingBoxDirty = false;
         }
 
         BoundingBox copyBox = this.modelBoundingBox.copy();
 
        copyBox.transform(this.worldTransform);
         this.boundingBox = copyBox;
     }
 
     @Override
     protected void updateState(Map<GlobalStateType, Stack<GlobalState>> stateMap) {
         Set<GlobalStateType> keys = stateMap.keySet();
         for (GlobalStateType stateType : keys) {
             this.globalStates.put(stateType, stateMap.get(stateType).peek());
         }
     }
 
 
 
     @Override
     public void draw(SceneRenderer renderer) {
         renderer.drawGeometry(this);
     }
 
     public Map<GlobalStateType, GlobalState> getStates() {
         return this.globalStates;
     }
 }
