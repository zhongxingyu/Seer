 package plia.core.scene;
 
 import plia.core.GameObject;
 import plia.math.Matrix4;
 import plia.math.Vector3;
 import plia.math.Vector4;
 
 public final class PlaneCollider extends Collider
 {
 	private final Vector4[] p = new Vector4[4];
 	private final Vector4[] corner = new Vector4[4];
 	
 	public PlaneCollider()
 	{
 		for (int i = 0; i < p.length; i++)
 		{
 			p[i] = new Vector4();
 			corner[i] = new Vector4();
 		}
 		this.updateCorner();
 	}
 	
 	@Override
 	protected void copyTo(GameObject gameObject)
 	{
 		super.copyTo(gameObject);
 		
 		PlaneCollider b = (PlaneCollider) gameObject;
 		for (int i = 0; i < p.length; i++)
 		{
 			b.p[i] = this.p[i].clone();
 			b.corner[i] = this.corner[i].clone();
 		}
 	}
 
 	@Override
 	public PlaneCollider instantiate()
 	{
 		PlaneCollider copy = new PlaneCollider();
 		this.copyTo(copy);
 		return copy;
 	}
 	
 	@Override
 	protected void onUpdateHierarchy(boolean parentHasChanged)
 	{
 		boolean hc = this.hasChanged;
 		
 		super.onUpdateHierarchy(parentHasChanged);
 		
 		if(isActive() && (hc || parentHasChanged))
 		{
			Vector3 scl = getScale();
			Matrix4 sm = new Matrix4(scl.x, 0, 0, 0, 0, scl.y, 0, 0, 0, 0, scl.z, 0, 0, 0, 0, 1);
			Matrix4 w = Matrix4.multiply(getWorldMatrix(), sm);
 			
 			for (int i = 0; i < p.length; i++)
 			{
				Matrix4.multiply(p[i], w, corner[i]);
 			}
 		}
 	}
 
 	@Override
 	public void setScale(Vector3 scale)
 	{
 		super.setScale(scale);
 		this.updateCorner();
 	}
 	
 	private void updateCorner()
 	{
 		float ex = localScaling.x / 2f;
 		float ey = localScaling.y / 2f;
 
 		corner[0].set(-ex, -ey, 0, 1);
 		corner[1].set(-ex, ey, 0, 1);
 		corner[2].set(ex, ey, 0, 1);
 		corner[3].set(ex, -ey, 0, 1);
 	}
 	
 	Vector4 getP0()
 	{
 		return p[0];
 	}
 	
 	Vector4 getP1()
 	{
 		return p[1];
 	}
 	
 	Vector4 getP2()
 	{
 		return p[2];
 	}
 	
 	Vector4 getP3()
 	{
 		return p[3];
 	}
 	
 	public Vector3 getPoint(int index)
 	{
 		return new Vector3(p[index].x, p[index].y, p[index].z);
 	}
 }
