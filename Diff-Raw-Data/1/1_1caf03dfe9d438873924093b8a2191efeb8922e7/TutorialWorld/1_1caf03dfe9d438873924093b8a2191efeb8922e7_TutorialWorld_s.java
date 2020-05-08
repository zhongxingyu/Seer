 package com.mobi.badvibes.model.world;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 import com.mobi.badvibes.Point;
 import com.mobi.badvibes.controller.GameMaster;
 import com.mobi.badvibes.model.localstorage.LocalStorage;
 import com.mobi.badvibes.model.people.NormanTheNormal;
 import com.mobi.badvibes.model.people.Person;
 import com.mobi.badvibes.model.people.logic.ExploreLogic;
 import com.mobi.badvibes.model.people.logic.HappyLogic;
 import com.mobi.badvibes.model.people.logic.LeavingTrainLogic;
 import com.mobi.badvibes.model.people.logic.PauseLogic;
 import com.mobi.badvibes.model.people.logic.PersonLogic;
 import com.mobi.badvibes.model.people.logic.RushLogic;
 import com.mobi.badvibes.util.GameUtil;
 import com.mobi.badvibes.util.MathHelper;
 import com.mobi.badvibes.util.MediaPlayer;
 import com.mobi.badvibes.view.PersonView;
 import com.mobi.badvibes.view.TrainView.TrainState;
 import com.mobi.badvibes.view.WorldRenderer;
 
 public class TutorialWorld extends World
 {
 
     /**
      * This dictates the number of seconds before the train arrives.
      */
     private static final float ArrivalTime         = 12;
 
     /**
      * This dictates how much time is allocated for boarding, including the
      * opening and closing of doors.
      */
     private static final float BoardingTime        = 12;
     /**
      * The delay before the next train arrives.
      */
     private static final float NextTrainTime       = 10;
 
     /**
      * It takes 5 seconds to go back to the main menu screen.
      */
     private static final float BackToMenuDelay     = 7;
 
     /**
      * There is 2 second delay per bucket when people will board.
      */
     private static final float BoardDelayPerBucket = 1;
 
     private float              Timer               = 0;
 
     private int                BucketIndex         = 0;
 
     private static final float totalWait           = 26f;
 
     @Override
     public void initialize()
     {
         super.initialize();
     }
 
     public ArrayList<Person> createPeople()
     {
         ArrayList<Person> list = new ArrayList<Person>();
 
         for (int i = 0; i < trainRidersCount; i++)
         {
             Person n = new NormanTheNormal();
             list.add(n);
         }
 
         return list;
     }
 
     public ArrayList<Person> createPeopleInTrain()
     {
         ArrayList<Person> list = new ArrayList<Person>();
 
         for (int i = 0; i < trainLeaversCount; i++)
             list.add(new NormanTheNormal());
 
         return list;
     }
 
     @Override
     public void runEvent(EventType type)
     {
         switch (type)
         {
         case RUSH:
 
             // TODO: change this to per-bucket rush
 
             for (PersonView p : WorldRenderer.Instance.masterBucket.get(BucketIndex))
             {
                 Person px = p.getPerson();
 
                 if (this.peopleList.contains(px) == false)
                     continue;
                 PersonLogic logic = px.getLogic();
                 if (logic instanceof PauseLogic)
                     continue;
                 if (logic instanceof LeavingTrainLogic)
                     continue;
                 if (logic instanceof HappyLogic)
                     continue;
 
                 if (peopleList.contains(px))
                 {
                     px.setLogic(new RushLogic(px));
                 }
             }
             break;
         case ALIGHT:
 
             for (Person p : peopleInTrainList)
             {
                 Random r = new Random();
                 Point newPoint = (r.nextBoolean()) ? new Point(9, 0) : new Point(10, 0);
                 p.getView().setPosition(GameUtil.getPlatformVectorCentered(newPoint));
                 p.setLogic(new LeavingTrainLogic(p));
             }
             break;
         case EXPLORE:
 
             for (Person p : peopleList)
             {
                 p.setLogic(new ExploreLogic(p));
             }
             break;
         }
     }
 
     @Override
     public void update(float delta)
     {
         Timer += delta;
         currentWait += delta;
         totalTimer += delta;
         trainProgress = MathHelper.ClampF(currentWait / totalWait, 0, 1f);
         if (peopleInTrainList.size() == 0)
         {
             if (peopleList.size() == 0)
             {
                 // TODO This doesn't show.
                 setInfoText("Level success!", 2);
                 currentState = WorldState.END_GAME;
             }
         }
         switch (currentState)
         {
         case ENTERING:
             if (Timer >= ArrivalTime)
             {
                MediaPlayer.sfx("");
                 Timer = 0;
                 train.trainView.arriveTrain();
                 currentState = WorldState.ARRIVAL;
                 setInfoText("!!!", 3);
                 setPeopleInTrainList(createPeopleInTrain());
             }
             break;
         case ARRIVAL:
             if (train.trainView.currentState == TrainState.BOARDING)
             {
                 Timer = 0;
 
                 // TODO: manually let the player switch to this state, ie:
                 // showing the button, etc.
                 currentState = WorldState.BOARDING;
 
                 // we immediately do the first rushing, para less wait :3
                 runEvent(EventType.RUSH);
                 BucketIndex++;
             }
             break;
         case BOARDING:
 
             // TODO: trigger per-bucket rush in runEvent, when all buckets are
             // iterated
             // then change state to DEPARTURE
             if (Timer >= BoardDelayPerBucket)
             {
                 Timer = 0;
 
                 runEvent(EventType.RUSH);
                 BucketIndex++;
 
                 if (BucketIndex >= WorldRenderer.Instance.masterBucket.size())
                 {
                     BucketIndex = 0;
 
                     currentState = WorldState.DEPARTURE;
 
                     train.trainView.departTrain();
                 }
             }
             break;
         case DEPARTURE:
             if (Timer >= NextTrainTime)
             {
                 currentWait = Timer = 0;
                 currentState = WorldState.ENTERING;
                 
             }
             break;
         case END_GAME:
             if (Timer >= BackToMenuDelay)
             {
                 
 
                 float boundedHappiness = MathHelper.ClampF(happiness, 0, 1f);
                 
                 GameMaster.data.happiness = boundedHappiness;
                 GameMaster.data.totalTime = totalTimer;
                 float timeFactor = (totalTimer > 45) ? totalTimer / 120f : 0;
                 GameMaster.data.score = MathHelper.ClampF(boundedHappiness - timeFactor, 0, 1f);
                 
                 LocalStorage.Write(GameMaster.data);
                 GameMaster.endGame();
             }
             break;
 
         default:
             break;
         }
     }
 }
