 package fi.cie.chiru.servicefusionar;
 
 import gl.Renderable;
 
 import javax.microedition.khronos.opengles.GL10;
 
 import commands.Command;
 
 import util.Log;
 import util.Vec;
 import worldData.AbstractObj;
 import worldData.Updateable;
 import worldData.Visitor;
 
 public class ServiceApplication extends AbstractObj 
 {
 	private static final String LOG_TAG = "ServiceApplication";
 	private GDXMesh gdxMesh;
 	private String name;
 	
 	public ServiceApplication(String name)
 	{
 		Log.d(LOG_TAG, "Creating application " + name);
 		this.name = name;
 	}
 	
 	public String getName()
 	{
 		return this.name;
 	}
     //should return MeshComponent?
 	public GDXMesh getMesh() {
 		return gdxMesh;
 	}
 
 	public void setMesh(GDXMesh gdxMesh) 
 	{
 		this.gdxMesh = gdxMesh;
 	}
 	
 	public Vec getPosition() 
 	{
 		return gdxMesh.getPosition();
 	}
 
 	public void setPosition(Vec position) 
 	{
 		this.gdxMesh.setPosition(position);
 	}
 
 	public void setRotation(Vec rotation) 
 	{
		this.gdxMesh.setRotation(rotation);
 	}
 	
 	public Vec getRotation() 
 	{
 		return gdxMesh.getRotation();
 	}
 	
 	public void setScale(Vec scale) 
 	{
 		this.gdxMesh.setScale(scale);
 	}
 	
 	public Vec getScale() 
 	{
 		return gdxMesh.getScale();
 	}
 
 	@Override
 	public boolean update(float timeDelta, Updateable parent) 
 	{
 		// TODO Auto-generated method stub
 		return true;
 	}
 
 	@Override
 	public boolean accept(Visitor visitor) 
 	{
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public void render(GL10 gl, Renderable parent) 
 	{
 		gdxMesh.render(gl, parent);
 		
 	}
 
 	@Override
 	public Command getOnClickCommand() 
 	{
         return gdxMesh.getOnClickCommand();
 	}
 
 	@Override
 	public void setOnClickCommand(Command c) 
 	{
         gdxMesh.setOnClickCommand(c);
 	}
 	
 }
