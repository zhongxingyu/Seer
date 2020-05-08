 package dacus.core.publisher.impl;
 
 import deus.core.publisher.RemoteCalledPublisher;
 import deus.core.publisher.impl.AbstractPublisherStub;
 import deus.model.pub.SubscriberMetadata;
 import deus.model.sub.PublisherMetadata;
 import deus.model.user.id.UserId;
 import deus.model.user.id.UserIdType;
 
 public class LocalPublisherStub<Id extends UserId> extends AbstractPublisherStub<Id> {
 
	private RemoteCalledPublisher<Id> publisher;

 
 	public LocalPublisherStub(PublisherMetadata<Id> publisherMetadata) {
 		super(publisherMetadata);
 		assert(publisherMetadata.getUserId().getType().equals(UserIdType.local));
 	}
 
 	
 	@Override
 	public void addObserver(SubscriberMetadata<Id> subscriberMetadata) {
		// FIXME: get publisher using publisherMetadata		
 		publisher.addObserver(subscriberMetadata);
 	}
 
 
 	@Override
 	public void deleteObserver(SubscriberMetadata<Id> subscriberMetadata) {
		// FIXME: get publisher using publisherMetadata
 		publisher.deleteObserver(subscriberMetadata);
 	}
 
 
 }
