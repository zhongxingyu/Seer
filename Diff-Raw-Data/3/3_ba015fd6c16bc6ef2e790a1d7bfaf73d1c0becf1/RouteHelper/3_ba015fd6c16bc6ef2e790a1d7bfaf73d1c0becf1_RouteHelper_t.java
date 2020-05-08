 package com.bastengao.struts2.freeroute.helper;
 
 import com.bastengao.struts2.freeroute.annotation.MethodType;
 import com.bastengao.struts2.freeroute.annotation.Route;
 
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 /**
  * @author bastengao
  * @date 13-3-2 10:41
  */
 public class RouteHelper {
 
     public static Route mockRoute(String value, MethodType[] methods, String[] params) {
         Route route = mock(Route.class);
         when(route.value()).thenReturn(value);
         when(route.method()).thenReturn(methods);
         when(route.params()).thenReturn(params);
        when(route.interceptors()).thenReturn(new String[]{});
         return route;
     }
 
     public static Route mockRoute(String value) {
         Route route = mock(Route.class);
         when(route.value()).thenReturn(value);
         when(route.method()).thenReturn(new MethodType[]{});
         when(route.params()).thenReturn(new String[]{});
        when(route.interceptors()).thenReturn(new String[]{});
         return route;
     }
 }
