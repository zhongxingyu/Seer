 package net.mklew.hotelms.inhouse.web.modules.views.top;
 
 import net.mklew.hotelms.domain.room.Room;
 import net.mklew.hotelms.domain.room.RoomName;
 import net.mklew.hotelms.domain.room.RoomType;
 import org.objectledge.context.Context;
 import org.objectledge.i18n.I18nContext;
 import org.objectledge.modules.views.BasicLedgeTopView;
 import org.objectledge.parameters.Parameters;
 import org.objectledge.pipeline.ProcessingException;
 import org.objectledge.templating.TemplatingContext;
 import org.objectledge.web.HttpContext;
 import org.objectledge.web.mvc.MVCContext;
 
 import java.util.*;
 
 /**
  * @author Marek Lewandowski <marek.m.lewandowski@gmail.com>
  * @since 11/3/12
  *        Time: 1:32 PM
  */
 public class NewReservation extends BasicLedgeTopView
 {
     /**
      * Constructs a builder instance.
      *
      * @param context application context for use by this builder.
      */
     public NewReservation(Context context)
     {
         super(context);
     }
 
     @Override
     public void process(Parameters parameters, TemplatingContext templatingContext, MVCContext mvcContext, HttpContext httpContext, I18nContext i18nContext) throws ProcessingException
     {
         Collection<RoomType> roomTypes = new ArrayList<>();
         // TODO fetch room types from repository
         RoomType luxury = new RoomType("luxury");
         RoomType cheap = new RoomType("cheap");
         RoomType niceOne = new RoomType("nice one");
 
         roomTypes.addAll(Arrays.asList(luxury, cheap, niceOne));
         templatingContext.put("roomTypes", roomTypes);
 
         // TODO fetch rooms from repository
         // TODO add comparable to Room and compare it by room names
         // TODO pass Set (so its ordered) to templatingContext instead of collection
        Room L100 = new Room(luxury, new RoomName("100", "L"));
        Room L101 = new Room(luxury, new RoomName("101", "L"));
        Room L102 = new Room(luxury, new RoomName("102", "L"));
        Room C103 = new Room(cheap, new RoomName("103", "C"));
        Room C104 = new Room(cheap, new RoomName("104", "C"));
        Room N105 = new Room(niceOne, new RoomName("105", "N"));
 
         Collection<Room> rooms = Arrays.asList(L100, L101, L102, C103, C104, N105);
         templatingContext.put("rooms", rooms);
     }
 }
