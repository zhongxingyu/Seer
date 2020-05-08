 package polly.rx.commands;
 
 import polly.rx.MSG;
 import polly.rx.core.orion.Orion;
 import polly.rx.core.orion.PathPlanner;
 import polly.rx.core.orion.PathPlanner.RouteOptions;
 import polly.rx.core.orion.PathPlanner.UniversePath;
 import polly.rx.core.orion.model.Sector;
 import polly.rx.core.orion.model.Wormhole;
 import polly.rx.httpv2.OrionController;
 import de.skuzzle.polly.sdk.DelayedCommand;
 import de.skuzzle.polly.sdk.MyPolly;
 import de.skuzzle.polly.sdk.Parameter;
 import de.skuzzle.polly.sdk.Signature;
 import de.skuzzle.polly.sdk.Types;
 import de.skuzzle.polly.sdk.Types.TimespanType;
 import de.skuzzle.polly.sdk.User;
 import de.skuzzle.polly.sdk.exceptions.CommandException;
 import de.skuzzle.polly.sdk.exceptions.DuplicatedSignatureException;
 import de.skuzzle.polly.sdk.exceptions.InsufficientRightsException;
 import de.skuzzle.polly.sdk.time.Milliseconds;
 
 
 public class RouteCommand extends DelayedCommand {
 
     public RouteCommand(MyPolly polly) throws DuplicatedSignatureException {    
         super(polly, "route", (int) Milliseconds.fromSeconds(10)); //$NON-NLS-1$
         this.createSignature(MSG.routeSig0Desc, 
                 OrionController.ROUTE_ORION_PREMISSION, 
                 new Parameter(MSG.routeSig0Start, Types.STRING), 
                 new Parameter(MSG.routeSig0Ziel, Types.STRING));
         this.setHelpText(MSG.routeHelp);
     }
     
     
     
     @Override
     protected boolean executeOnBoth(User executer, String channel, Signature signature)
             throws CommandException, InsufficientRightsException {
         
         if (this.match(signature, 0)) {
             final String s = signature.getStringValue(0);
             final String t = signature.getStringValue(1);
             
             final Sector start = this.parse(s);
             final Sector target = this.parse(t);
             
             final PathPlanner planner = Orion.INSTANCE.getPathPlanner();
             final RouteOptions options = new RouteOptions(new TimespanType(0), 
                     new TimespanType(0));
             final UniversePath path = planner.findShortestPath(start, target, options);
             this.outputPath(channel, path);
         }
         
         return super.executeOnBoth(executer, channel, signature);
     }
     
     
     
     private void outputPath(String channel, UniversePath path) {
         if (!path.pathFound()) {
             this.reply(channel, MSG.routeNoRouteFound);
             return;
         }
         for (final Wormhole hole : path.getWormholes()) {
            final String s = String.format("von: %s %d,%d nach: %s %d,%d. Entladung: %d-%d. Triebwerke: %s",  //$NON-NLS-1$
                     hole.getSource().getQuadName(), hole.getSource().getX(), hole.getSource().getY(), 
                     hole.getTarget().getQuadName(), hole.getTarget().getX(), hole.getTarget().getY(), 
                     hole.getMinUnload(), hole.getMaxUnload(),
                     hole.requiresLoad());
             this.reply(channel, s);
         }
         this.reply(channel, MSG.bind(MSG.routeInfo, path.getSectorJumps(), 
                 path.getQuadJumps()));
     }
     
     
     
     private Sector parse(String s) throws CommandException {
         // parse backwards
         try {
             int i = s.lastIndexOf(' ');
             final int y = Integer.parseInt(s.substring(i + 1));
             s = s.substring(0, i);
             i = s.lastIndexOf(' ');
             final int x = Integer.parseInt(s.substring(i + 1));
             final String quadName = s.substring(0, i);
             return Orion.INSTANCE.createQuadrantProvider().getQuadrant(quadName).getSector(x, y);
         } catch (Exception e) {
             throw new CommandException(MSG.routeParseError, e);
         }
     }
 
 }
