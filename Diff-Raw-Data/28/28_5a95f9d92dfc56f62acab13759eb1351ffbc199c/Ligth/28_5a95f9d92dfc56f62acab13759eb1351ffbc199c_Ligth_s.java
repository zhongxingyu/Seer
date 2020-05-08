 package net.joesoft.andoria.gfx;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.Mesh;
 import com.badlogic.gdx.graphics.VertexAttribute;
 import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.math.Vector3;
 import net.joesoft.andoria.model.MoveableObject;
 import net.joesoft.andoria.utils.Log;
 import net.joesoft.andoria.utils.MeshGenerator;
 import net.joesoft.andoria.utils.VertexBuffer;
 
 public class Ligth extends MoveableObject {
 	private Log LOG = new Log(this.getClass());
 	private final Mesh mesh;
 
 	public Ligth() {
		Gdx.gl10.glShadeModel(GL10.GL_SMOOTH);
		Gdx.gl10.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, new float[]{1, position.x, position.y, position.z}, 0);
		Gdx.gl10.glLightfv(GL10.GL_LIGHT0, GL10.GL_AMBIENT, new float[]{1, 1, 1, 1}, 0);
		Gdx.gl10.glLightfv(GL10.GL_LIGHT0, GL10.GL_DIFFUSE, new float[]{1, 1, 1, 1}, 0);

 		final VertexAttributes attributes = new VertexAttributes(
 			new VertexAttribute(VertexAttributes.Usage.Position, 3, null),
 			new VertexAttribute(VertexAttributes.Usage.ColorPacked, 4, null),
 			new VertexAttribute(VertexAttributes.Usage.Normal, 3, null)
 		);
 
		final VertexBuffer buffer = MeshGenerator.generateCube();
 		buffer.setAttributes(attributes);
 		buffer.addColors(Color.WHITE.toFloatBits());
 		buffer.calculateNormals();
		mesh = new Mesh(true, buffer.getBufferSize(), 0, buffer.getAttributes());
 		mesh.setVertices(buffer.toFloatArray());
		mesh.scale(0.3f, 0.3f, 0.3f);
	}

	public void move(float x, float y, float z) {
		final Vector3 newPosition = super.moveObject(x, y, z);
		Gdx.gl10.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, new float[]{newPosition.x, newPosition.y, newPosition.z, 1}, 0);
 	}
 
 	public void on() {
 		Gdx.gl.glEnable(GL10.GL_LIGHTING);
 		Gdx.gl.glEnable(GL10.GL_LIGHT0);
 	}
 
 	public void off() {
 		Gdx.gl.glDisable(GL10.GL_LIGHTING);
 		Gdx.gl.glDisable(GL10.GL_LIGHT0);
 	}
 
 	public void render() {
		Gdx.gl.glDisable(GL10.GL_LIGHTING);
 		Gdx.gl10.glPushMatrix();
 		Gdx.gl10.glTranslatef(position.x, position.y, position.z);
 		mesh.render(GL10.GL_TRIANGLES);
 		Gdx.gl10.glPopMatrix();
 	}
 }
