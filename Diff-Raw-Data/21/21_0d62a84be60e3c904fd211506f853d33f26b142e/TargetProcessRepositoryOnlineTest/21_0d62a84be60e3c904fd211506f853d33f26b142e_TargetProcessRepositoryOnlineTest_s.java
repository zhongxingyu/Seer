 package com.switchfly.targetprocess.plugin;
 
import com.intellij.mock.MockApplication;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.tasks.Comment;
import com.intellij.tasks.Task;
import com.intellij.tasks.TaskRepository;
import com.intellij.tasks.TaskType;
import com.intellij.tasks.config.TaskSettings;
import com.switchfly.targetprocess.TargetProcessIcons;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.alternatives.AbstractDelegatingMutablePicoContainer;

import static org.junit.Assert.*;

public class TargetProcessRepositoryOnlineTest {
 
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
 }
