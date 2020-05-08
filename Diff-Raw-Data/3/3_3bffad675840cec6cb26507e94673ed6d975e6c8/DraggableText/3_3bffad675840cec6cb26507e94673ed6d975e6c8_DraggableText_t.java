 package fi.cie.chiru.servicefusionar.serviceApi;
 
 import gl.GLFactory;
 import gl.scenegraph.MeshComponent;
 import util.IO;
 import util.Vec;
 import android.content.ClipData;
 import android.content.ClipData.Item;
 import android.graphics.Color;
 import android.graphics.Typeface;
 import android.util.Log;
 import android.view.View;
 import android.view.View.DragShadowBuilder;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import commands.ui.CommandInUiThread;
 
 public class DraggableText
 {
 
 	private static final String LOG_TAG = "TextPopUp";
 	private String text;
 	private String manager;
 	private TextView tv;
 	private ServiceManager serviceManager = null;
 	private Vec position = null;
 	private boolean textVisible  = false;
 	private boolean textCreated  = false;
 	private boolean attachedToCamera = false;
 	private MeshComponent textComponent = null;
 
 	public DraggableText(ServiceManager serviceManager) 
 	{
 		this.serviceManager = serviceManager;
 		position = new Vec();
 
 	}
 	
 	public void  setDragText(String text)
 	{
 		this.text = text;
 	}
 	
 	public void  setDragTextManager(String manager)
 	{
 		this.manager = manager;
 	}
 	
 	public void setPosition(Vec position)
 	{
 		this.position.setToVec(position);
 	}
 	
 	public void visible() 
 	{
 		if(!textCreated)
 		{
 			createTextComponent();
 		}
 		
 		if(!textVisible)
 		{	
 			serviceManager.getSetup().world.add(textComponent);
 			textVisible = true;
 		}
 		else
 		{
 			serviceManager.getSetup().world.remove(textComponent);
 			textVisible = false;
 		}
 	}
 	
 	public void attachToCamera(boolean attach)
 	{
 		this.attachedToCamera = attach;
 		if (textComponent == null)
 		{
 			Log.i(LOG_TAG, "TextComponent was NULL in attachToCamera function.");
 			return;
 		}
 		
 		if(attach)
 		{
 			serviceManager.getSetup().camera.attachToCamera(textComponent);
 		}
 		else
 		{
 			serviceManager.getSetup().camera.detachFromCamera(textComponent);
 		}
 	}
 	
 	public void createTextComponent()
 	{
 	    tv = new TextView(serviceManager.getSetup().myTargetActivity);
 	    tv.setId(generateUniqueId());
 	    tv.setText(this.text);
 	    tv.setTextColor(Color.BLACK);
 	    tv.setBackgroundColor(Color.LTGRAY);
 	    tv.setTextSize(25);
 	    tv.setTypeface(Typeface.MONOSPACE);
 	    textComponent = GLFactory.getInstance().newTexturedSquare(this.text, IO.loadBitmapFromView(tv));
 	    //serviceManager.getSetup().world.add(textComponent);
 	    
 	    textComponent.setPosition(this.position);
 	    textComponent.setRotation(new Vec(90.0f, 0.0f, 180.0f));
 	    textComponent.setScale(new Vec(0.5f, 1.0f, 1.0f));
 	    textComponent.setOnLongClickCommand(new DragTextPopUpObject(tv));
 	    
	    textCreated = true;
 	    attachToCamera(attachedToCamera);
 	}
 	
 	private int generateUniqueId()
 	{
 		int id = 0;
 		RelativeLayout root = (RelativeLayout) serviceManager.getSetup().getGuiSetup().getMainContainerView();
 		
 		while(root.findViewById(++id) != null );
 		    return id;
 	}
 
 	private class DragTextPopUpObject extends CommandInUiThread 
 	{
 		private TextView v;
 		
 		public DragTextPopUpObject(TextView DraggedItem)
 		{
 		    v = DraggedItem;
 		}
 		
 	//	@Override
 		public void executeInUiThread()
 		{
 
 			RelativeLayout root = (RelativeLayout) serviceManager.getSetup().getGuiSetup().getMainContainerView();
 //			if(root.findViewById(v.getId())== null)
 //			    root.addView(v);
 			
 			root.removeView(v);
 			root.addView(v);
 
 		    v.setVisibility(View.GONE);
 		    
 //		    if(text.isEmpty())
 //		    {
 		    	text = (String)v.getText();
 //		    }
 			
 			View.DragShadowBuilder shadow = new DragShadowBuilder(v);
 			ClipData data = ClipData.newPlainText("DragData", text);
 			ClipData.Item i = new ClipData.Item(manager);
 			data.addItem(i);
 			v.startDrag(data, shadow, null, 0);
 		}
 		
 	}
 }
