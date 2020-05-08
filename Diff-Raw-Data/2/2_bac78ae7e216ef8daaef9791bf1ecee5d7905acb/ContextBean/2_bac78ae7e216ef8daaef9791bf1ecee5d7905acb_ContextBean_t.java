 package com.barchart.util.common.flow.provider;
 
 import com.barchart.util.common.flow.api.Context;
 import com.barchart.util.common.flow.api.Event;
 import com.barchart.util.common.flow.api.State;
 
 /**
  * Context implementation.
  */
 class ContextBean<E extends Event<?>, S extends State<?>, A> implements
 		Context<E, S, A> {
 
 	static class Builder<E extends Event<?>, S extends State<?>, A> implements
 			Context.Builder<E, S, A> {
 
 		final FlowBean<E, S, A> flow;
 
 		volatile A attachment;
 
 		volatile E initialEvent;
 
 		volatile S initialState;
 
 		public Builder(final FlowBean<E, S, A> flow) {
 			this.flow = flow;
 		}
 
 		@Override
 		public Context.Builder<E, S, A> initial(final E event) {
 			this.initialEvent = event;
 			return this;
 		}
 
 		@Override
 		public Context.Builder<E, S, A> initial(final S state) {
 			this.initialState = state;
 			return this;
 		}
 
 		@Override
 		public Context<E, S, A> build(final A attachment) {
 			if (attachment == null) {
 				throw new NullPointerException("Missing attachment.");
 			}
 			this.attachment = attachment;
 			return new ContextBean<E, S, A>(this);
 		}
 
 	}
 
 	static <E extends Event<?>, S extends State<?>, A> //
 	Context.Builder<E, S, A> newBuilder(final FlowBean<E, S, A> flow) {
 		return new Builder<E, S, A>(flow);
 	}
 
 	final FlowBean<E, S, A> flow;
 
 	final A attachment;
 
 	volatile E event;
 	volatile S state;
 
 	volatile E internalEvent;
 
 	ContextBean(final Builder<E, S, A> builder) {
 
 		flow = builder.flow;
 
 		attachment = builder.attachment;
 
 		/** Ensure initial event. */
 		if (builder.initialEvent == null) {
 			event = flow.initialEvent;
 		} else {
 			event = builder.initialEvent;
 		}
 
 		/** Ensure initial state. */
 		if (builder.initialState == null) {
 			state = builder.flow.initialState;
 		} else {
 			state = builder.initialState;
 		}
 
 	}
 
 	@Override
 	public void fire(final E event) {
 		internalEvent = event;
		flow.fire(event, this);
 	}
 
 	@Override
 	public A attachment() {
 		return attachment;
 	}
 
 	@Override
 	public E event() {
 		return event;
 	}
 
 	/**
 	 * Replace current event.
 	 */
 	E event(final E next) {
 		final E past = this.event;
 		this.event = next;
 		return past;
 	}
 
 	@Override
 	public S state() {
 		return state;
 	}
 
 	/**
 	 * Replace current state.
 	 */
 	S state(final S next) {
 		final S past = this.state;
 		this.state = next;
 		return past;
 	}
 
 	@Override
 	public String toString() {
 		return "(" + event() + "," + state() + ") " + attachment;
 	}
 
 }
