 package com.PsichiX.JustIDS;
 
 import android.util.Log;
 
 import com.PsichiX.XenonCoreDroid.XeApplication.*;
 import com.PsichiX.XenonCoreDroid.Framework.Graphics.*;
 import com.PsichiX.XenonCoreDroid.XeUtils.*;
 import com.PsichiX.XenonCoreDroid.XeSense;
 
 public class GameState extends State implements CommandQueue.Delegate
 {
 	
 	/** od 0 do 100*/
 	public static double manaLevel;
 	public static double healthLevel;
 	
 	private Camera2D _cam;
 	private Scene _scn;
 	private CommandQueue _cmds = new CommandQueue();
 	private float _lastForce = 0.0f;
 	private float _currentForce = 0.0f;
 	private Sprite Mana;
 	private Sprite Health;
 	
 	@Override
 	public void onEnter()
 	{
 		_cmds.setDelegate(this);
 		
 		_scn = (Scene)getApplication().getAssets().get(R.raw.scene, Scene.class);
 		_cam = (Camera2D)_scn.getCamera();
 		_cam.setViewPosition(_cam.getViewWidth() * 0.5f, _cam.getViewHeight() * 0.5f);
 
 		
 		Material mat;
 		mat = (Material) getApplication().getAssets().get(R.raw.logo_material, Material.class);
 		Mana = new Sprite(mat);
 		float tabRed[] =  {1.0f, 0.0f, 0.0f, 1.0f};
 		Mana.getProperties().setVec("uColor", tabRed);
 		Mana.setSize(_cam.getViewWidth() * 0.5f, _cam.getViewHeight());
 		_scn.attach(Mana);
 				
 		Health = new Sprite(mat);
 		float tabBlue[] = {0.0f, 0.0f, 1.0f, 1.0f};
 		Health.getProperties().setVec("uColor", tabBlue);
 		Health.setSize(_cam.getViewWidth() * 0.5f, _cam.getViewHeight());
 		Health.setPosition( _cam.getViewWidth() * 0.5f, 0.0f);
 		_scn.attach(Health);
 	}
 	
 	@Override
 	public void onExit()
 	{
 		_scn.releaseAll();
 	}
 	
 	@Override
 	public void onInput(Touches ev)
 	{
 		Touch t = ev.getTouchByState(Touch.State.DOWN);
 		if (t != null)
 		{
 			getApplication().pushState(new ResultState());
 		}
 	}
 	
 	@Override
 	public void onSensor(XeSense.EventData ev)
 	{
 		if(ev.type == XeSense.Type.LINEAR_ACCELERATION)
 		{
 			_lastForce = _currentForce;
 			_currentForce = MathHelper.vecLength(ev.values[0], ev.values[1], ev.values[2]);
			//Log.d("ACCEL", Float.toString(_currentForce));
			if(_currentForce > 1.0f && _lastForce <= 1.0f)
 				_cmds.queueCommand(this, "StartAttack", null);
			else if(_lastForce > 1.0f && _currentForce <= 1.0f)
 				_cmds.queueCommand(this, "StopAttack", null);
 		}
 	}
 
 	@Override
 	public void onUpdate()
 	{
 		getApplication().getSense().setCoordsOrientation(-1);
 		
 		float dt = getApplication().getTimer().getDeltaTime() * 0.001f;
 		//float dt = 1.0f / 30.0f;
 		
 		_cmds.run();
 		_scn.update(dt);  
 		
 		healthLevel = 80;
 		manaLevel = 20;
 		
 		Mana.setSize(_cam.getViewWidth() * 0.5f, (float) (healthLevel * _cam.getViewHeight() * 0.01f));
 		Health.setSize(_cam.getViewWidth() * 0.5f, (float) (manaLevel * _cam.getViewHeight() * 0.01f));
 	}
 	
 	public void onCommand(Object sender, String cmd, Object data)
 	{
 		if(cmd.equals("StartAttack"))
 			Log.d("ATTACK", "START");
 		else if(cmd.equals("StopAttack"))
 			Log.d("ATTACK", "STOP");
 	}
 }
