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
 	private float _maxForce = 0.0f;
 	private boolean _forceRecording = false;
 	private Sprite Mana;
 	private Sprite Health;
 	private GameStateManager gsm;
 	
 	public GameState(GameStateManager gsm) {
 		this.gsm = gsm;
 	}
 	
 	@Override
 	public void onEnter()
 	{
 		_cmds.setDelegate(this);
 		
 		_scn = (Scene)getApplication().getAssets().get(R.raw.scene, Scene.class);
 		_cam = (Camera2D)_scn.getCamera();
 		_cam.setViewPosition(_cam.getViewWidth() * 0.5f, _cam.getViewHeight() * 0.5f);
 		_cam.setViewAngle(180);
 		
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
 		gsm.resetGame();
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
			getApplication().pushState(new ResultState("You won!"));
		}
	}
	
 	@Override
 	public void onSensor(XeSense.EventData ev)
 	{
 		if(ev.type == XeSense.Type.LINEAR_ACCELERATION)
 		{
 			_lastForce = _currentForce;
 			_currentForce = MathHelper.vecLength(ev.values[0], ev.values[1], ev.values[2]);
 			if(_forceRecording)
 				_maxForce = Math.max(_currentForce, _maxForce);
 			//Log.d("ACCEL", Float.toString(_maxForce));
 			if(_currentForce > 20.0f && _lastForce <= 20.0f)
 				_cmds.queueCommand(this, "StartAttack", null);
 			else if(_lastForce > 20.0f && _currentForce <= 20.0f)
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
 		Mana.setSize(_cam.getViewWidth() * 0.5f, (float) (healthLevel * _cam.getViewHeight() * 0.01f));
 		Health.setSize(_cam.getViewWidth() * 0.5f, (float) (manaLevel * _cam.getViewHeight() * 0.01f));
 	}
 	
 	public void onCommand(Object sender, String cmd, Object data)
 	{
 		if(cmd.equals("StartAttack"))
 		{
 			Log.d("ATTACK", "START");
 			_maxForce = 0.0f;
 			_forceRecording = true;
 		}
 		else if(cmd.equals("StopAttack"))
 		{
 			Log.d("ATTACK", "STOP");
 			double strength = calculateStrength();
 			gsm.attackWithStrength(strength);
 			manaLevel = 0.0f;
 			_forceRecording = false;
 			_maxForce = 0.0f;
 		}
 	}
 
 	private double calculateStrength() {
 		double strength = 30 * Math.max(Math.min(1,(_maxForce- 20.0)/10.0),0)*(manaLevel/100.0); 
 		return strength;
 	}
 	
 	public void youWon() {
 		getApplication().pushState(new ResultState("You won!"));
 	}
 	
 	public void youLost() {
 		getApplication().pushState(new ResultState("You lost!"));
 	}
 
 }
 
