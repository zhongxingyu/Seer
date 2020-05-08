 package com.google.gwt.sample.contacts.client.presenter;
 
 import com.google.gwt.event.shared.EventBus;
 import com.google.gwt.event.shared.SimpleEventBus;
 import com.google.gwt.sample.contacts.client.view.ListContactsView;
 import com.google.gwt.sample.contacts.shared.ContactDetails;
 import com.google.gwt.sample.contacts.shared.ContactsServiceAsync;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import java.util.ArrayList;
 import java.util.List;
 import junit.framework.TestCase;
 import org.easymock.IAnswer;
 import static org.easymock.EasyMock.createStrictMock;
 import static org.easymock.EasyMock.expectLastCall;
 import static org.easymock.EasyMock.getCurrentArguments;
 import static org.easymock.EasyMock.isA;
 import static org.easymock.EasyMock.replay;
 import static org.easymock.EasyMock.verify;
 
 @SuppressWarnings( "unchecked" )
 public class ExampleJRETest
   extends TestCase
 {
   private ContactsPresenter _contactsPresenter;
   private ContactsServiceAsync _mockRpcService;
   private EventBus _mockEventBus;
   private ListContactsView _mockViewList;
   private List<ContactDetails> _contactDetails;
 
   protected void setUp()
   {
     _mockRpcService = createStrictMock( ContactsServiceAsync.class );
     _mockEventBus = new SimpleEventBus();
     _mockViewList = createStrictMock( ListContactsView.class );
    _contactsPresenter = new ContactsPresenter( _mockRpcService, _mockEventBus, _mockViewList );
   }
 
   public void testDeleteButton()
   {
     _contactDetails = new ArrayList<ContactDetails>();
     _contactDetails.add( new ContactDetails( "0", "1_contact" ) );
     _contactDetails.add( new ContactDetails( "1", "2_contact" ) );
     _contactsPresenter.setContactDetails( _contactDetails );
 
     _mockRpcService.deleteContacts( isA( ArrayList.class ), isA( AsyncCallback.class ) );
 
     expectLastCall().andAnswer( new IAnswer()
     {
       public Object answer()
         throws Throwable
       {
         final AsyncCallback callback = getCallback();
         callback.onSuccess( null );
         return null;
       }
     } );
 
     _mockRpcService.getContactDetails( isA( AsyncCallback.class ) );
     expectLastCall().andAnswer( new IAnswer()
     {
       public Object answer()
         throws Throwable
       {
        final ArrayList<ContactDetails> results = new ArrayList<ContactDetails>();
        results.add( new ContactDetails( "0", "1_contact" ) );
        getCallback().onSuccess( results );
         return null;
       }
     } );
 
 
     replay( _mockRpcService );
     _contactsPresenter.onDeleteButtonClicked();
     verify( _mockRpcService );
 
     assertEquals( 1, _contactsPresenter.getContactDetails().size() );
   }
 
   private AsyncCallback getCallback()
   {
     final Object[] arguments = getCurrentArguments();
     return (AsyncCallback) arguments[ arguments.length - 1 ];
   }
 }
