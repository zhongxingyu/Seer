 package com.switchfly.targetprocess.plugin;
 
public class TargetProcessRepositoryOnlineTest {   /* it works, but due maven dependencies problem better comment for now
 
     private final TargetProcessRepository repository;
 
     public TargetProcessRepositoryOnlineTest() {
         Disposable disposable = new Disposable() {
             @Override
             public void dispose() {
             }
         };
         Application mockApplication = new MockApplication(disposable) {
             @NotNull
             @Override
             public MutablePicoContainer getPicoContainer() {
                 return new AbstractDelegatingMutablePicoContainer(null) {
                     @Override
                     public MutablePicoContainer makeChildContainer() {
                         return null;
                     }
 
                     @Override
                     public Object getComponentInstance(Object componentKey) {
                         return new TaskSettings();
                     }
                 };
             }
         };
         ApplicationManager.setApplication(mockApplication, disposable);
 
         repository = new TargetProcessRepository();
         repository.setUrl("http://md5.tpondemand.com/");
     }
 
     @Test
     public void testCreateCancellableConnection() {
         TaskRepository.CancellableConnection cancellableConnection = repository.createCancellableConnection();
         assertNull(cancellableConnection.call());
     }
 
     @Test
     public void testFindTask() throws Exception {
         Task task = repository.findTask("335");
         assertNotNull(task);
         assertEquals("335", task.getId());
         assertEquals(TaskType.FEATURE, task.getType());
         assertSame(TargetProcessIcons.UserStory, task.getIcon());
         assertEquals("GoGo Back project", task.getProject());
         assertEquals("http://md5.tpondemand.com/RestUI/TpView.aspx?id=335", task.getIssueUrl());
 
         Comment[] comments = task.getComments();
         assertTrue(comments.length >= 1);
         Comment comment = comments[0];
         assertEquals("Administrator Administrator", comment.getAuthor());
         assertEquals("I am not a happy camper", comment.getText());
     }
    */
 }
