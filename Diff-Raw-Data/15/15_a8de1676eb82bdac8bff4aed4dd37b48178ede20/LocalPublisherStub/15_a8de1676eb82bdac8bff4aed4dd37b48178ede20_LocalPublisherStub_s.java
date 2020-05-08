 package dacus.core.publisher.impl;
 
 import deus.core.publisher.RemoteCalledPublisher;
 import deus.core.publisher.impl.AbstractPublisherStub;
 import deus.core.publisher.impl.PublisherImpl;
 import deus.model.pub.SubscriberMetadata;
 import deus.model.sub.PublisherMetadata;
 import deus.model.user.id.UserId;
 import deus.model.user.id.UserIdType;
 
 public class LocalPublisherStub<Id extends UserId> extends AbstractPublisherStub<Id> {
 
 
 	public LocalPublisherStub(PublisherMetadata<Id> publisherMetadata) {
 		super(publisherMetadata);
 		assert(publisherMetadata.getUserId().getType().equals(UserIdType.local));
 	}
 
 	
 	@Override
 	public void addObserver(SubscriberMetadata<Id> subscriberMetadata) {
 		// TODO: how to get publisher?
 		//RemoteCalledPublisher<Id> publisher = new RemoteCalledPublisherImpl<Id>(getPublisherMetadata());
		RemoteCalledPublisher publisher = new PublisherImpl<Id>(getPublisherMetadata());
 		publisher.addObserver(subscriberMetadata);
 	}
 
 
 	@Override
 	public void deleteObserver(SubscriberMetadata<Id> subscriberMetadata) {
 		// TODO: how to get publisher?
 		//RemoteCalledPublisher<Id> publisher = new RemoteCalledPublisherImpl<Id>(getPublisherMetadata());
		RemoteCalledPublisher publisher = new PublisherImpl<Id>(getPublisherMetadata());
 		publisher.deleteObserver(subscriberMetadata);
 	}
 
 
 }
