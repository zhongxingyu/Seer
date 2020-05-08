 package net.cheney.motown.router;
 
import net.cheney.motown.common.api.Request;
 
 public abstract class Constraint {
 
 	public abstract boolean matches(Request request);
 }
