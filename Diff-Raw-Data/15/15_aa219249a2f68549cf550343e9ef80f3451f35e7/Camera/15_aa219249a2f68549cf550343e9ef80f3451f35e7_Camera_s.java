 import processing.core.PApplet;
 import processing.core.PVector;
 
 
 public class Camera extends Node {
 	public PVector center;
 	public PVector forward;
 	public PVector right;
 	public PVector up;
 	public float fov;
 	
 	public Camera( Vertex vertex_, PApplet parent_ ) {
 		super( vertex_, parent_ );
 		this.fov = PApplet.radians( 60 );
 		
 		this.center = PVector.add( this.pos, this.vertex.getD() );
 		
 		this.forward = PVector.sub( this.center, this.pos );
 		this.forward.normalize();
 		
 		this.up = new PVector( 0, 1, 0 );
 		
 		this.right = this.up.cross(this.forward);
 	}
 	
 	public void active() {
 		float width = parent.width;
 		float height = parent.height;
 		float cameraZ = ( height / 2 ) / PApplet.tan( this.fov / 2 );
 		
 		parent.camera( this.pos.x, this.pos.y, this.pos.z,
 				this.center.x, this.center.y, this.center.z, 
 				this.up.x, this.up.y, this.up.z );
 		parent.perspective( fov, width / height, cameraZ / 10, cameraZ * 10 );
 	}
 }
