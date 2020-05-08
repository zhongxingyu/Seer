 package com.barchart.netty.client.base;
 
 import io.netty.channel.ChannelPipeline;
 import rx.Observable;
 
 import com.barchart.netty.client.facets.AuthenticationAware;
 import com.barchart.netty.client.facets.AuthenticationFacet;
 import com.barchart.netty.client.pipeline.AuthenticationHandler;
 import com.barchart.netty.client.transport.TransportProtocol;
 
 /**
  * An authenticating Connectable proxy that authenticates a user with the host.
  * The actual authentication communication is implementation-dependent via a
  * provided AuthenticationHandler. MessageFlowHandler is a recommended base
  * class for writing authentication handlers in order to block downstream
  * handlers from receiving channelActive() events until authentication is
  * complete.
  * 
  * @param <A> The account object type
  */
 public abstract class AuthenticatingConnectableBase<T extends AuthenticatingConnectableBase<T, A>, A>
 		extends KeepaliveConnectableBase<T> implements AuthenticationAware<A> {
 
 	protected abstract static class Builder<B extends Builder<B, C, D>, C extends AuthenticatingConnectableBase<C, D>, D>
 			extends KeepaliveConnectableBase.Builder<B, C> {
 
 		protected AuthenticationHandler.Builder<D> builder;
 
 		@SuppressWarnings("unchecked")
 		public B authenticator(final AuthenticationHandler.Builder<D> builder_) {
 			builder = builder_;
 			return (B) this;
 		}
 
 		@Override
 		protected C configure(final C client) {
 			super.configure(client);
 			client.facet = new AuthenticationFacet<D>(builder);
 			return client;
 		}
 
 	}
 
	private AuthenticationFacet<A> facet = null;
 
 	protected AuthenticatingConnectableBase(final TransportProtocol transport_) {
 		super(transport_);
 	}
 
 	@Override
 	public void initPipeline(final ChannelPipeline pipeline) throws Exception {
 		super.initPipeline(pipeline);
 		facet.initPipeline(pipeline);
 	}
 
 	@Override
 	public Observable<AuthState> authStateChanges() {
 		return facet.authStateChanges();
 	}
 
 	@Override
 	public AuthState authState() {
 		return facet.authState();
 	}
 
 	@Override
 	public A account() {
 		return facet.account();
 	}
 
 }
