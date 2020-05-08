 package com.zhaoyunhe.pw.props.properties;
  
 import info.u250.c2d.engine.Engine;
 
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.g2d.NinePatch;
 import com.badlogic.gdx.graphics.g2d.TextureAtlas;
 import com.badlogic.gdx.scenes.scene2d.ui.Label;
 import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
 import com.badlogic.gdx.scenes.scene2d.ui.Slider;
 import com.badlogic.gdx.scenes.scene2d.ui.Slider.SliderStyle;
 import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
 import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
 
 public abstract class BodyProperties extends EditorProperties{
 	final Slider density;
 	final Slider friction;
 	final Slider restitution;
 //	final Slider filter_categoryBits;
 //	final Slider filter_groupIndex;
 //	final Slider filter_maskBits;
 //	final CheckBox sensor;
 //	final CheckBox dynamic;
 //	final Slider angle;
 //	final Slider centerX;
 //	final Slider centerY;
 //	final Slider name;
 //	final Slider res;
 //	final Slider userData;
 	
 	
 	public BodyProperties(){
 //		name = new TextField("", skin);
 //		res = new TextField("", skin);
 //		userData = new TextField("", skin);
 		TextureAtlas atlas=Engine.resource("atlas");
 		SliderStyle style=new SliderStyle();
 //		style.background=new TextureRegionDrawable(atlas.findRegion("slider_bg"));
 		style.background=new NinePatchDrawable(new NinePatch(atlas.findRegion("slider_bg")));
 		style.knob=new TextureRegionDrawable(atlas.findRegion("slider_knob"));
 		
 		LabelStyle labelStyle=new LabelStyle(Engine.getDefaultFont(), new Color(1, 1, 1, 1));
 		
 		density=new Slider(0, 1, 0.01f, false, style);
 		friction = new Slider(0, 1, 0.01f, false, style);
 		restitution = new Slider(0, 1, 0.01f, false, style);
 //		filter_categoryBits = new TextField("", skin) ;
 //		filter_groupIndex = new TextField("", skin) ;
 //		filter_maskBits = new TextField("",  skin) ;
 //		sensor = new CheckBox("sensor",skin) ;
 //		dynamic = new CheckBox("dynamic",skin) ;
 //		angle = new TextField("angle", skin) ;
 //		centerX = new TextField("centerX", skin) ;
 //		centerY = new TextField("centerY", skin) ;
 		this.left();
 		
 //		this.add(new Label("name:", skin));
 //		this.add(this.name).colspan(3).fillX();
 //		this.row();
 //		this.add(new Label("centerX", skin));
 //		this.add(this.centerX).colspan(3).fillX();
 //		this.row();
 //		this.add(new Label("centerY", skin));
 //		this.add(this.centerY).colspan(3).fillX();
 //		this.row();
 //		this.add(new Label("angle", skin));
 //		this.add(this.angle).colspan(3).fillX();
 //		this.row();
 //		this.add(new Label("res:", skin));
 //		this.add(this.res).colspan(3).fillX();
 //		this.row();
 //		this.add(new Label("userData:", skin));
 //		this.add(this.userData).colspan(3).fillX();
 //		this.row();
 		
		this.row();
 		this.add(new Label("density", labelStyle));
		this.add(this.density);
 		this.add(new Label("friction", labelStyle));
 		this.add(this.friction);
		this.row();
 		this.add(new Label("restitution", labelStyle));
 		this.add(this.restitution);
		this.row();
 		
 //		this.add(new Label("sensor", labelStyle));
 //		this.add(this.sensor);
 //		this.add(new Label("dynamic", labelStyle));
 //		this.add(this.dynamic);
 //		this.row();
 	
 		
 //		this.add(new Label("groupIndex", labelStyle));
 //		this.add(this.filter_groupIndex).width(40);
 //		this.add(new Label("maskBits", labelStyle));
 //		this.add(this.filter_maskBits).width(40);
 //		this.row();
 //		this.add(new Label("categoryBits", labelStyle));
 //		this.add(this.filter_categoryBits).width(40);
 //		this.row();
 		
 	}
 	
 	
 	public void update(Object data){
 //		bind(data, "name", name);
 //		bind(data, "data", userData);
 //		bind(data, "res", res);
 //		bind(data, "isSensor", sensor);
 //		bind(data, "isDynamic", dynamic);
 		bind(data, "density", density);
 		bind(data, "friction", friction);
 		bind(data, "restitution", restitution);
 //		bind(data, "angle", angle);
 //		bind(BodyData.class.cast(data).center, "x", centerX);
 //		bind(BodyData.class.cast(data).center, "y", centerY);
 //		bind(data, "filter_categoryBits",filter_categoryBits );
 //		bind(data, "filter_groupIndex", filter_groupIndex);
 //		bind(data, "filter_maskBits", filter_maskBits);
 	}
 }
