 package org.vikenpedia.fellesprosjekt.server;
 
 import java.io.IOException;
 import java.sql.SQLException;
 
 import org.vikenpedia.fellesprosjekt.shared.Command;
 import org.vikenpedia.fellesprosjekt.shared.Command.Commands;
 import org.vikenpedia.fellesprosjekt.shared.ConnectionHandler;
 import org.vikenpedia.fellesprosjekt.shared.NetServer;
 import org.vikenpedia.fellesprosjekt.shared.Network;
 import org.vikenpedia.fellesprosjekt.shared.models.FpCalendar;
 import org.vikenpedia.fellesprosjekt.shared.models.Meeting;
 import org.vikenpedia.fellesprosjekt.shared.models.MeetingParticipant;
 import org.vikenpedia.fellesprosjekt.shared.models.ReplyStatus;
 import org.vikenpedia.fellesprosjekt.shared.models.Room;
 import org.vikenpedia.fellesprosjekt.shared.models.User;
 
 import com.google.gson.Gson;
 
 public class Server implements NetServer {
     public static void main(String[] args) {
         try {
             @SuppressWarnings("unused") Server s = new Server(2948);
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     private class ConHan implements ConnectionHandler {
         private Server srv;
         private Gson packer;
         private User user;
 
         public ConHan(Server srv, Gson packer) {
             this.srv = srv;
             this.packer = packer;
         }
 
         @Override
         public Command command(Command comm) {
             switch (comm.command) {
             case LOGIN:
                 return switchLOGIN(comm);
             case SAVE_MEETING:
                 return switchSAVE_MEETING(comm);
             case SAVE_MEETINGPARTICIPANT:
                 return switchSAVE_MEETINGPARTICIPANT(comm);
             case GET_MY_CALENDARS:
                 return switchGET_MY_CALENDARS();
             case GET_WEEK:
                 return switchGET_WEEK(comm);
             case GET_INVITES:
                 return switchGET_INVITES();
             case ACCEPT_INVITE:
                 return switchACCEPT_INVITE(comm);
             case DECLINE_INVITE:
                 return switchDECLINE_INVITE(comm);
             case DELETE_MEETING:
                 return switchDELETE_MEETING(comm);
             case GET_FREE_ROOMS:
                 return switchGET_FREE_ROOMS(comm);
             case BOOK_ROOM:
                 return switchBOOK_ROOM(comm);
             case ADD_MEETING:
                 return switchADD_MEETING(comm);
             case GET_INVITED:
                 return switchGET_INVITED(comm);
             case GET_UNINVITED:
                 return switchGET_UNINVITED(comm);
             case INVITE:
                 return switchINVITE(comm);
 
             }
             return null;
         }
 
         private Command switchLOGIN(Command comm) {
             Command cmd = new Command();
             User user = srv.db.login(comm.strarg1, comm.strarg2);
             if (user != null) {
                 this.user = user;
                 cmd.command = Commands.LOGIN_COMPLETE;
             } else {
                 cmd.command = Commands.LOGIN_FAILED;
             }
             return cmd;
         }
 
         private Command switchSAVE_MEETING(Command comm) {
             // implementer sjekk av om bruker som kaller save_meeting faktisk er
             // chairman
             Command cmd = new Command();
             Meeting meeting = packer.fromJson(comm.data, Meeting.class);
             if (srv.db.saveMeeting(meeting)) {
                 cmd.command = Commands.OK;
             } else
                 cmd.command = Commands.ERROR;
             return cmd;
         }
 
         private Command switchSAVE_MEETINGPARTICIPANT(Command comm) {
             Command cmd = new Command();
             MeetingParticipant meetingPar = packer.fromJson(comm.data, MeetingParticipant.class);
             if (srv.db.saveMeetingParticipant(meetingPar)) {
                 cmd.command = Commands.OK;
             } else
                 cmd.command = Commands.ERROR;
             return cmd;
         }
 
         private Command switchGET_MY_CALENDARS() {
             Command cmd = new Command(Commands.CALENDARS);
             FpCalendar[] callArray = srv.db.getUserCalendars(user.getId());
             cmd.data = packer.toJson(callArray);
             return cmd;
         }
 
         private Command switchGET_WEEK(Command comm) {
             Command cmd = new Command(Commands.MEETINGS);
             int calendarId = comm.intarg1;
             int weekNr = comm.intarg2;
             Meeting[] meetings = srv.db.getMeetingsInWeek(calendarId, weekNr, user.getId());
             cmd.data = packer.toJson(meetings);
             return cmd;
         }
 
         private Command switchGET_INVITES() {
             Command cmd = new Command(Commands.MEETINGS);
             Meeting[] meetings = srv.db.getUnrepliedMeetings(user.getId());
             cmd.data = packer.toJson(meetings);
             return cmd;
         }
 
         private Command switchACCEPT_INVITE(Command comm) {
             return internalSetReply(comm, true);
         }
 
         private Command switchDECLINE_INVITE(Command comm) {
             return internalSetReply(comm, false);
         }
 
         private Command internalSetReply(Command comm, boolean accept) {
             Command cmd = new Command();
             MeetingParticipant meetPar = srv.db.getMeetingParticipant(comm.intarg1, user.getId());
             if (meetPar == null) {
                 cmd.command = Commands.ERROR;
                 cmd.strarg1 = "is null";
                 return cmd;
             }
             if (accept) {
                 meetPar.setReplyStatus(ReplyStatus.YES);
             } else
                 meetPar.setReplyStatus(ReplyStatus.NO);
             if (srv.db.saveMeetingParticipant(meetPar))
                 cmd.command = Commands.OK;
             else {
                 cmd.command = Commands.ERROR;
                 cmd.strarg2 = "could not save meetingparticipant";
             }
             return cmd;
         }
 
         private Command switchDELETE_MEETING(Command comm) {
             Command cmd = new Command();
             int meetingId = comm.intarg1;
             if (isChairman(meetingId)) {
                 if (srv.db.deleteMeetingChairman(meetingId))
                     cmd.command = Commands.OK;
                 else
                     cmd.command = Commands.ERROR;
                 return cmd;
             } else {
                 return switchDECLINE_INVITE(comm);
             }
         }
 
         private boolean isChairman(int meetingId) {
             return (this.user.getId() == srv.db.getChairmanId(meetingId));
         }
 
         private Command switchGET_FREE_ROOMS(Command comm) {
             Command cmd = new Command(Commands.ROOMS);
             Room[] rooms = srv.db.getFreeRooms(comm.intarg1);
             cmd.data = packer.toJson(rooms);
             return cmd;
         }
 
         private Command switchBOOK_ROOM(Command comm) {
             Command cmd = new Command();
             if (roomStillFree(comm.intarg1, comm.intarg2)) {
                 if (srv.db.createReservationEntry(comm.intarg2, comm.intarg1))
                     cmd.command = Commands.OK;
                 else
                     cmd.command = Commands.ERROR;
             } else
                 cmd.command = Commands.ERROR;
             return cmd;
         }
 
         private boolean roomStillFree(int roomId, int meetingId) {
             Room[] rooms = srv.db.getFreeRooms(meetingId);
             for (int i = 0; i < rooms.length; i++) {
                 if (rooms[i].getId() == roomId)
                     return true;
             }
             return false;
         }
 
         private Command switchADD_MEETING(Command comm) {
             Command cmd = new Command();
             Meeting meeting = packer.fromJson(comm.data, Meeting.class);
             if (srv.db.userOwnsCalendar(user.getId(), comm.intarg1)) {
                 if (srv.db.createMeeting(meeting, comm.intarg1, user.getId())) {
                     cmd.command = Commands.OK;
                 } else {
                     cmd.command = Commands.ERROR;
                 }
             } else {
                 cmd.command = Commands.ERROR;
             }
             return cmd;
         }
 
         private Command switchGET_INVITED(Command comm) {
             Command cmd = new Command(Commands.MEETINGPARTICIPANTS);
             MeetingParticipant[] meetingParticipants = srv.db.getInvited(comm.intarg1);
             cmd.data = packer.toJson(meetingParticipants);
             return cmd;
         }
 
         private Command switchGET_UNINVITED(Command comm) {
             Command cmd = new Command(Commands.MEETINGPARTICIPANTS);
             FpCalendar[] uninvitedUsers = srv.db.getNotInvited(comm.intarg1);
             cmd.data = packer.toJson(uninvitedUsers);
             return cmd;
         }
 
         private Command switchINVITE(Command comm) {
             Command cmd = new Command();
            int meetingId = cmd.intarg1;
            int calendarId = cmd.intarg2;
            System.out.println("meetingId: " + Integer.toString(meetingId));
            System.out.println("calendarId: " + Integer.toString(calendarId));
             if (srv.db.createUserHasMeetingEntry(calendarId, meetingId, ReplyStatus.WAITING) && (srv.db.createCalendarHasMeetingEntry(meetingId, calendarId)))
                 cmd.command = Commands.OK;
             else
                 cmd.command = Commands.ERROR;
             return cmd;
         }
 
     }
 
     private Network net;
     private DBQuery db;
 
     public Server(int port) throws IOException {
         this.net = new Network(port, this);
         try {
             this.db = new DBQuery();
         } catch (ClassNotFoundException e) {
             e.printStackTrace();
             System.exit(1);
         } catch (SQLException e) {
             e.printStackTrace();
             System.exit(1);
         }
         this.net.run();
     }
 
     @Override
     public ConnectionHandler makeConnectionHandler(Gson packer) {
         return new ConHan(this, packer);
     }
 
 }
