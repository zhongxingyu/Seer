 package com.sk.methods.action.structure;
 
import org.powerbot.script.lang.Identifiable;
import org.powerbot.script.lang.Validatable;
 import org.powerbot.script.wrappers.Component;
 
 import com.sk.SkMethodContext;
 
 public interface BarIcon extends Validatable, Identifiable {
 
 	public Component getComponent(SkMethodContext ctx);
 
 	public static final BarIcon NIL = Ability.NIL;
 }
