 package de.nordakademie.nakjava.server.internal.model;
 
 import java.lang.annotation.ElementType;
 import java.lang.annotation.Retention;
 import java.lang.annotation.RetentionPolicy;
 import java.lang.annotation.Target;
 
 import de.nordakademie.nakjava.gamelogic.stateMachineEvenNewer.states.State;
 
 @Target(value = { ElementType.FIELD })
 @Retention(value = RetentionPolicy.RUNTIME)
 public @interface VisibleField {
 	public de.nordakademie.nakjava.gamelogic.cards.impl.Target[] targets();
 
 	public State[] states();
 }
