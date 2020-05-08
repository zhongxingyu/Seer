 package collabeditsync;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.intellij.openapi.editor.Document;
 import com.intellij.openapi.editor.impl.DocumentImpl;
 import com.intellij.openapi.editor.impl.event.DocumentEventImpl;
 import net.sf.json.JSONObject;
 import net.sf.json.JSONSerializer;
 import org.junit.Before;
 import org.junit.Test;
 
 import javax.print.Doc;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.mockito.Mockito.*;
 
 public class CollabEditTest {
 
     private CollabEdit collabEdit = new CollabEdit();
 
     @Before
     public void setUp() throws Exception {
         collabEdit.resource = mock(CollabEditResource.class);
     }
 
     @Test
     public void modificationContainsBothInsertAndDeleteCommandsWithOffsets() throws Exception {
         String data = "{\"op\":{\"ops\":[[7,1],[9,\"zxxxx\"],[8,\"b\"],[7,20]],\"cuid\":745713,\"parent_hash\":\"b6c963fa53e083f41389c8478b88057c\",\"result_hash\":\"e001a5db8a6f1f16b5c457779c2156a0\"}}";
         JSONObject jsonData = (JSONObject) JSONSerializer.toJSON(data);
         when(collabEdit.resource.waitForUpdate(false)).thenReturn(jsonData);
 
         Command command = collabEdit.waitForModificationCommand();
         assertEquals(1, command.delete.startOffset);
         assertEquals(6, command.delete.endOffset);
         assertEquals(1, command.insert.offset);
         assertEquals("b", command.insert.text);
     }
 
     @Test
     public void modificationContainsOnlyInsertWithOffsets() throws Exception {
         String data = "{\"op\":{\"ops\":[[7,1],[8,\"b\"],[7,20]],\"cuid\":745713,\"parent_hash\":\"b6c963fa53e083f41389c8478b88057c\",\"result_hash\":\"e001a5db8a6f1f16b5c457779c2156a0\"}}";
         JSONObject jsonData = (JSONObject) JSONSerializer.toJSON(data);
         when(collabEdit.resource.waitForUpdate(false)).thenReturn(jsonData);
 
         Command command = collabEdit.waitForModificationCommand();
         assertNull(command.delete);
         assertEquals(1, command.insert.offset);
         assertEquals("b", command.insert.text);
     }
 
 
     @Test
     public void modificationContainsOnlyDeleteWithOffsets() throws Exception {
         String data = "{\"op\":{\"ops\":[[7,1],[9,\"zxxxx\"],[7,20]],\"cuid\":745713,\"parent_hash\":\"b6c963fa53e083f41389c8478b88057c\",\"result_hash\":\"e001a5db8a6f1f16b5c457779c2156a0\"}}";
         JSONObject jsonData = (JSONObject) JSONSerializer.toJSON(data);
         when(collabEdit.resource.waitForUpdate(false)).thenReturn(jsonData);
 
         Command command = collabEdit.waitForModificationCommand();
         assertEquals(1, command.delete.startOffset);
         assertEquals(6, command.delete.endOffset);
         assertNull(command.insert);
     }
 
     @Test
     public void modificationContainsOnlyDeleteWithEndOffset() throws Exception {
         String data = "{\"op\":{\"ops\":[[9,\"zxxxx\"],[7,20]],\"cuid\":745713,\"parent_hash\":\"b6c963fa53e083f41389c8478b88057c\",\"result_hash\":\"e001a5db8a6f1f16b5c457779c2156a0\"}}";
         JSONObject jsonData = (JSONObject) JSONSerializer.toJSON(data);
         when(collabEdit.resource.waitForUpdate(false)).thenReturn(jsonData);
 
         Command command = collabEdit.waitForModificationCommand();
         assertEquals(0, command.delete.startOffset);
         assertEquals(5, command.delete.endOffset);
     }
 
     @Test
     public void waitForDataUntilOpsReceived() throws Exception {
         String firstData = "{\"lang\":\"none\",\"messages\":[{\"message_text\":\"renamed document to dd\",\"nickname\":\"Jevgeni\",\"type\":5}],\"name\":\"dd\",\"user_list\":[\"bob\"]}";
         String secondData = "{\"op\":{\"ops\":[[9,\"zxxxx\"],[7,20]],\"cuid\":745713}}";
         JSONObject firstJsonData = (JSONObject) JSONSerializer.toJSON(firstData);
         JSONObject secondJsonData = (JSONObject) JSONSerializer.toJSON(secondData);
         when(collabEdit.resource.waitForUpdate(false)).thenReturn(firstJsonData).thenReturn(secondJsonData);
 
         Command command = collabEdit.waitForModificationCommand();
         assertNotNull(command.delete);
     }
 
     @Test
     public void applyModificationToDocument() throws Exception {
         Command command = new Command(new Command.Delete(3, "qwerty"), new Command.Insert(3, "test"));
         Document document = mock(Document.class);
         command.apply(document);
 
         verify(document).deleteString(3, 9);
         verify(document).insertString(3, "test");
     }
 
     @Test
     public void sendUpdatesOnEvent() throws Exception {
         Document document = mock(Document.class);
         when(document.getCharsSequence()).thenReturn("12345xx89");
         DocumentEventImpl event = new DocumentEventImpl(document, 5, "67", "xx", System.currentTimeMillis(), false);
 
         collabEdit.handle(event);
         verify(collabEdit.resource).sendUpdate(5, "67", "xx", "12345xx89");
     }
 
     @Test
     public void sendUpdatesOnlyForDifferences() throws Exception {
         collabEdit.update("x", "x");
         verifyZeroInteractions(collabEdit.resource);
     }
 
     @Test
     public void sendFullUpdates() throws Exception {
         collabEdit.update("old-text", "new-text");
        verify(collabEdit).update("old-text", "new-text");
     }
 
     @Test
     public void requestFullSyncIfProblemsWithUpdate() throws Exception {
          doThrow(new UnsuccessfulResponseException()).doNothing()
                  .when(collabEdit.resource).sendUpdate(anyString(), anyString());
 
         JSONObject jsonObject = new JSONObject();
         jsonObject.accumulate("full_text", "xxx");
         when(collabEdit.resource.waitForUpdate(true)).thenReturn(jsonObject);
 
         collabEdit.update("old-text", "new-text");
 
         verify(collabEdit.resource).waitForUpdate(true);
         verify(collabEdit.resource).sendUpdate("old-text", "new-text");
         verify(collabEdit.resource).sendUpdate("xxx", "new-text");
     }
 
     // TODO: remove if document is empty?
 //
 //   Received: {"op":{"ops":[[9,"p"]],"cuid":745865,"parent_hash":"83878c91171338902e0fe0fb97a8c47a","result_hash":"d41d8cd98f00b204e9800998ecf8427e"}}
 //[ 906781]  ERROR - pplication.impl.LaterInvocator - Wrong endOffset: 1; documentLength: 0
 //java.lang.IndexOutOfBoundsException: Wrong endOffset: 1; documentLength: 0
 //	at com.intellij.openapi.editor.impl.DocumentImpl.b(DocumentImpl.java:410)
 //	at com.intellij.openapi.editor.impl.DocumentImpl.deleteString(DocumentImpl.java:345)
 
 }
