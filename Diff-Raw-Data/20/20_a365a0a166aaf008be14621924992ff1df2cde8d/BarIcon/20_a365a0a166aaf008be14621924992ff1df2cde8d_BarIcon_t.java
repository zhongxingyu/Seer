 package com.sk.methods.action.structure;
 
 import org.powerbot.script.wrappers.Component;
import org.powerbot.script.wrappers.Identifiable;
import org.powerbot.script.wrappers.Validatable;
 
 import com.sk.SkMethodContext;
 
 public interface BarIcon extends Validatable, Identifiable {
 
 	public Component getComponent(SkMethodContext ctx);
 
 	public static final BarIcon NIL = Ability.NIL;
 }
