 package dungeon.load.adapters;
 
 import dungeon.models.KillQuest;
 import dungeon.models.Quest;
 
 import javax.xml.bind.annotation.adapters.XmlAdapter;
 
 public class QuestAdapter extends XmlAdapter<QuestAdapter, Quest> {
   public String type;
 
   public int id;
 
   public String name;
 
   public String text;
 
   public boolean done;
 
   public String roomId;
 
   @Override
   public Quest unmarshal (QuestAdapter adapter) throws Exception {
    if (adapter.type.equals("KILL_QUEST")) {
       return new KillQuest(adapter.id, adapter.name, adapter.text, adapter.done, adapter.roomId);
     }
 
     return null;
   }
 
   @Override
   public QuestAdapter marshal (Quest quest) throws Exception {
     QuestAdapter adapter = new QuestAdapter();
     adapter.id = quest.getId();
     adapter.name = quest.getName();
     adapter.text = quest.getText();
     adapter.done = quest.isDone();
 
     if (quest instanceof KillQuest) {
       adapter.type = "KILL_QUEST";
       adapter.roomId = ((KillQuest)quest).getRoomId();
     }
 
     return adapter;
   }
 }
