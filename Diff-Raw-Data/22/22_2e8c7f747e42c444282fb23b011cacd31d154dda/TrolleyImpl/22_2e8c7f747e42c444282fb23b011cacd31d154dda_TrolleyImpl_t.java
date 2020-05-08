 package com.euroit.militaryshop.bean.impl;
 
 import org.springframework.context.annotation.Scope;
 import org.springframework.context.annotation.ScopedProxyMode;
 import org.springframework.stereotype.Component;
 import org.springframework.web.context.WebApplicationContext;
 
 import com.euroit.eshop.bean.impl.BaseTrolleyImpl;
 import com.euroit.eshop.dto.BaseTrolleyItemDto;
 
 @Component("trolley")
 @Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
 public class TrolleyImpl extends BaseTrolleyImpl<BaseTrolleyItemDto> {
 
 	private static final long serialVersionUID = -6173083074170758649L;
 	
}
