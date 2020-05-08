 /*
  * Copyright (c) <2013> <Jim Johnson jimj@jimj.net>
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 
 package net.jimj.automaton.commands;
 
 import com.mongodb.BasicDBObject;
 import com.mongodb.DBCollection;
 import com.mongodb.DBCursor;
 import com.mongodb.DBObject;
 import net.jimj.automaton.events.DataStoredEvent;
 import net.jimj.automaton.events.MessageEvent;
 import net.jimj.automaton.model.User;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 public class NoteCommand extends Command implements Processor {
     private static final Logger logger = LoggerFactory.getLogger(NoteCommand.class);
 
     private static final String NOTE_FROM = "from";
     private static final String NOTE_TO = "to";
     private static final String NOTE_NOTE = "note";
     private static final String NOTE_WHEN = "when";
     private static final String NOTE_DELIVERED = "delivered";
 
     private DBCollection notes;
 
     private static final SimpleDateFormat WHEN_FMT = new SimpleDateFormat("yyyy-MM-dd HH:mm zzz");
 
     public NoteCommand(DBCollection notes) {
         this.notes = notes;
         notes.ensureIndex(new BasicDBObject(NOTE_TO, 1));
     }
 
     @Override
     public String getCommandName() {
         return "note";
     }
 
     @Override
     public void execute(User user, String args) {
         String[] argParts = ArgUtil.split(args);
 
         if(argParts[0].isEmpty()) {
             logger.info("Finding notes for " + user.getNick());
             findNotes(user);
             return;
         }
 
         String toNick = argParts[0].trim();
         String note = ArgUtil.squash(argParts, 1);
         if(note == null ||  note.isEmpty()) {
             //insultEvent
             return;
         }
 
         logger.info("Storing note for " + toNick + " (" + note + ")");
         storeNote(user.getNick(), toNick, note);
         addEvent(new DataStoredEvent(user, "note"));
     }
 
     protected void storeNote(String from, String to, String note) {
         BasicDBObject noteObj = new BasicDBObject(NOTE_FROM, from);
         noteObj.append(NOTE_TO, to.toLowerCase());
         noteObj.append(NOTE_NOTE, note);
         noteObj.append(NOTE_DELIVERED, false);
         noteObj.append(NOTE_WHEN, System.currentTimeMillis());
         notes.insert(noteObj);
     }
 
     protected void findNotes(User to) {
        BasicDBObject query = new BasicDBObject(NOTE_TO, to.getNick().toLowerCase());
        query.append(NOTE_DELIVERED, false);
        DBCursor noteCursor = notes.find(query);
         if(noteCursor == null) {
             return;
         }
 
         while(noteCursor.hasNext()) {
             DBObject noteObj = noteCursor.next();
             String from = (String)noteObj.get(NOTE_FROM);
             String note = (String)noteObj.get(NOTE_NOTE);
             long when = (long)noteObj.get(NOTE_WHEN);
 
             StringBuilder noteMessage = new StringBuilder(to.getNick()).append(" you have a note from ");
             noteMessage.append(from).append(" at ").append(WHEN_FMT.format(new Date(when)));
             addEvent(new MessageEvent(to, noteMessage.toString()));
             addEvent(new MessageEvent(to, note));
 
             notes.update(new BasicDBObject("_id", noteObj.get("_id")),new BasicDBObject("$set",
                     new BasicDBObject(NOTE_DELIVERED, true)));
         }
     }
 
     @Override
     public boolean shouldProcess(String message) {
         return true;
     }
 
     @Override
     public void process(User user, String message) {
         findNotes(user);
     }
 
     @Override
     public void help(User user) {
        addEvent(new MessageEvent(user, "note <nick> <note text> - leaves a note for <nick> which will be " +
            "displayed the next time I see them active."));
        addEvent(new MessageEvent(user, "Only nicknames are currently supported.  Do not use real names."));
     }
 }
