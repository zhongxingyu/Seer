 package net.cheney.motown.router;
 
import net.cheney.http.core.api.Request;
 
 public abstract class Constraint {
 
 	public abstract boolean matches(Request request);
 }
