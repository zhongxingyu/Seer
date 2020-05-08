 package net.cheney.motown.router;
 
import net.cheney.http.core.api.Request;
import net.cheney.http.core.api.Response;
 
 public interface Callable {
 
 	Response call(Request request);
 }
