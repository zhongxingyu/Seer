 package com.alecgorge.minecraft.jsonapi.packets.netty.router;
 
 import net.minecraft.util.io.netty.buffer.Unpooled;
 import net.minecraft.util.io.netty.handler.codec.http.DefaultFullHttpResponse;
 import net.minecraft.util.io.netty.handler.codec.http.FullHttpResponse;
 import net.minecraft.util.io.netty.handler.codec.http.HttpResponseStatus;
 import net.minecraft.util.io.netty.handler.codec.http.HttpVersion;
 import net.minecraft.util.io.netty.util.CharsetUtil;
 
 import com.alecgorge.minecraft.jsonapi.JSONAPI;
 import com.alecgorge.minecraft.jsonapi.packets.netty.APIv2Handler;
 
 public class JSONAPIDefaultRoutes {
 	JSONAPI	api;
 
 	public JSONAPIDefaultRoutes(JSONAPI api) {
 		this.api = api;
 
 		RouteMatcher r = api.getRouter();
 		r.get("/api/2/call", new Handler<FullHttpResponse, RoutedHttpRequest>() {
 			@Override
 			public FullHttpResponse handle(RoutedHttpRequest event) {
 				APIv2Handler h = new APIv2Handler(event.request);
 
 				return h.serve();
 			}
 		});
 
 		r.get("/api/2/version", new Handler<FullHttpResponse, RoutedHttpRequest>() {
 			@Override
 			public FullHttpResponse handle(RoutedHttpRequest event) {
 				APIv2Handler h = new APIv2Handler(event.request);
 
 				return h.serve();
 			}
 		});
 
 		r.get("/", new Handler<FullHttpResponse, RoutedHttpRequest>() {
 			@Override
 			public FullHttpResponse handle(RoutedHttpRequest event) {
				return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.copiedBuffer("This is a Minecraft server. HTTP on this port by JSONAPI. JSONAPI by Alec Gorge.", CharsetUtil.UTF_8));
 			}
 		});
 	}
 }
