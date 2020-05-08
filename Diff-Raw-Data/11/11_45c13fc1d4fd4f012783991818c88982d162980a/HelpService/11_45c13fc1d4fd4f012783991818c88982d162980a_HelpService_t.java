 package net.madz.download.service.services;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map.Entry;
 
 import net.madz.download.service.IService;
 import net.madz.download.service.IServiceResponse;
 import net.madz.download.service.ServiceHub;
 import net.madz.download.service.annotations.Arg;
 import net.madz.download.service.annotations.Command;
 import net.madz.download.service.annotations.Option;
 import net.madz.download.service.requests.HelpRequest;
 import net.madz.download.service.responses.HelpResponse;
 
 @Command(commandName = "help", request = HelpRequest.class, arguments = { @Arg(name = "argCommandName",
         description = "command name, short name or full name are all ok.") }, options = {}, description = "Display all the commands and simple description.")
 public class HelpService implements IService<HelpRequest> {
 
     public static HelpService getInstance(String commandName) {
         if ( !commandName.equalsIgnoreCase(HelpService.class.getAnnotation(Command.class).commandName()) ) {
         }
         return (HelpService) ServiceHub.getInstance().getService(commandName);
     }
 
     @Override
     public void start() {
         // TODO Auto-generated method stub
     }
 
     @Override
     public boolean isStarted() {
         // TODO Auto-generated method stub
         return true;
     }
 
     @Override
     public void stop() {
         // TODO Auto-generated method stub
     }
 
     @Override
    public IServiceResponse processRequest(final HelpRequest request) {
         HelpResponse response = new HelpResponse();
         StringBuilder description = new StringBuilder();
         if ( null == request.getArgCommandName() || 0 >= request.getArgCommandName().length() ) {
             iterateAllCommands(response, description);
         } else {
             IService<?> service = null;
             service = ServiceHub.getService(request.getArgCommandName());
            if ( null == service ) {
                return new IServiceResponse() {
                    @Override
                    public String toString() {
                        return "Command " + request.getCommandName() + " does not exist.";
                    }
                };
            }
             Command command = service.getClass().getAnnotation(Command.class);
             description.append("NAME");
             description.append("\n");
             description.append("\t" + command.commandName() + " - ");
             description.append(command.description());
             description.append("\n");
             if ( command.options().length > 0 ) {
                 description.append("\n");
                 description.append("OPTIONS:");
                 description.append("\n");
                 for ( Option option : command.options() ) {
                     description.append("\t");
                     description.append(option.shortName());
                     description.append(", ");
                     description.append(option.fullName());
                     description.append("\n");
                     description.append("\t");
                     description.append("    ");
                     description.append(option.description());
                     description.append("\n");
                 }
             }
             if ( command.arguments().length > 0 ) {
                 description.append("\n");
                 description.append("ARGUMENTS:");
                 description.append("\n");
                 for ( Arg arg : command.arguments() ) {
                     description.append(arg.name());
                     description.append("\t");
                     description.append(arg.description());
                     description.append("\n");
                 }
             }
             response.setCommandName(request.getArgCommandName());
             response.setDescription(description.toString());
         }
         return response;
     }
 
     private void iterateAllCommands(HelpResponse response, StringBuilder description) {
         HashMap<String, IService> servicesregistry = ServiceHub.getServicesregistry();
         Iterator<Entry<String, IService>> iterator = servicesregistry.entrySet().iterator();
         while ( iterator.hasNext() ) {
             Entry<String, IService> next = iterator.next();
             String key = next.getKey();
             IService value = next.getValue();
             Command command = value.getClass().getAnnotation(Command.class);
             description.append(command.commandName());
             description.append("\t");
             description.append(command.description());
             description.append("\n");
         }
         response.setCommandName("Help");
         response.setDescription(description.toString());
     }
 }
