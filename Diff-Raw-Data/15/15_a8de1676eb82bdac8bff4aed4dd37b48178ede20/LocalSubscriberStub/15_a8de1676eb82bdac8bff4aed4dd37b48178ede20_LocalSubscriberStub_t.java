 package dacus.core.subscriber.impl;
 
 import deus.core.subscriber.RemoteCalledSubscriber;
 import deus.core.subscriber.Subscriber;
 import deus.core.subscriber.impl.AbstractSubscriberStub;
 import deus.core.subscriber.impl.SubscriberImpl;
 import deus.model.pub.SubscriberMetadata;
 import deus.model.sub.PublisherMetadata;
 import deus.model.user.id.UserId;
 import deus.model.user.id.UserIdType;
 
 // TODO: think about if this is needed. If it is nice to have this, remove the dependency of
 // /deus-core-puddle/src/main/java/deus/core/publisher/Publisher.java from the PublisherStub interface!
 // otherwise, PublisherImpl is also a PublisherStub, which is confusing!
 public class LocalSubscriberStub<Id extends UserId> extends AbstractSubscriberStub<Id> {
 
	//private RemoteCalledSubscriber<Id> subscriber;
 
 
 	public LocalSubscriberStub(SubscriberMetadata<Id> subscriberMetadata) {
 		super(subscriberMetadata);
 		assert (subscriberMetadata.getUserId().getType().equals(UserIdType.local));
 		// TODO: how to get subscriber
 		//subscriber = new SubscriberImpl<Id>(subscriberMetadata);
 	}
 
 
 	@Override
 	public void update(PublisherMetadata<Id> publisherMetadata, Object change) {
		//subscriber.update(publisherMetadata, change);
 	}
 
 
 }
