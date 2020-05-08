 package net.cheney.motown.router;
 
import net.cheney.motown.common.api.Request;
import net.cheney.motown.common.api.Response;
 
 public interface Callable {
 
 	Response call(Request request);
 }
