 package net.sf.jguard.core.authentication.callbackhandler;
 
 import net.sf.jguard.core.authentication.schemes.AuthenticationSchemeHandler;
 import net.sf.jguard.core.lifecycle.MockRequestAdapter;
 import net.sf.jguard.core.lifecycle.MockResponseAdapter;
 
 import java.util.Collection;
 
public class AsynchronousMockCallbackHandler extends AsynchronousJGuardCallbackHandler {
     public AsynchronousMockCallbackHandler(MockRequestAdapter request, MockResponseAdapter response, Collection<AuthenticationSchemeHandler<MockRequestAdapter, MockResponseAdapter>> authenticationSchemeHandlers) {
         super(request, response, authenticationSchemeHandlers);
     }
 
 }
