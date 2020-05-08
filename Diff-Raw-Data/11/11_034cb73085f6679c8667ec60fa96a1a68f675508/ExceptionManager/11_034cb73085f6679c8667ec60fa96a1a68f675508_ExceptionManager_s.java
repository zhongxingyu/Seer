 
 package com.quartercode.quarterbukkit.api.exception;
 
 import java.util.ArrayList;
 import java.util.List;
 import org.bukkit.plugin.Plugin;
 
 public class ExceptionManager {
 
     private static List<ExceptionHandler> exceptionHandlers = new ArrayList<ExceptionHandler>();
 
     /**
      * Returns all registered {@link ExceptionHandler}s.
      * 
      * @return The {@link ExceptionHandler}s.
      */
     public static List<ExceptionHandler> getExceptionHandlers() {
 
         return exceptionHandlers;
     }
 
     /**
      * Returns the registered {@link ExceptionHandler} for a {@link Plugin}.
      * 
      * @param plugin The binding {@link Plugin}.
      * @return The {@link ExceptionHandler}.
      */
     public static ExceptionHandler getExceptionHandler(final Plugin plugin) {
 
        for (final ExceptionHandler exceptionHandler : exceptionHandlers) {
             if (exceptionHandler.getPlugin().equals(plugin)) {
                 return exceptionHandler;
             }
         }
 
         throw new IllegalStateException("ExceptionHandler for plugin " + plugin.getName() + " not set");
     }
 
     /**
      * Registers the {@link ExceptionHandler} for a binding {@link Plugin}.
      * 
      * @param exceptionHandler The {@link ExceptionHandler} to register.
      */
     public static void setExceptionHandler(final ExceptionHandler exceptionHandler) {
 
        for (final ExceptionHandler exceptionHandler2 : exceptionHandlers) {
             if (exceptionHandler2.getPlugin().equals(exceptionHandler.getPlugin())) {
                 exceptionHandlers.remove(exceptionHandler2);
             }
         }
 
         exceptionHandlers.add(exceptionHandler);
     }
 
     /**
      * Unregisters the {@link ExceptionHandler} of a binding {@link Plugin}.
      * 
      * @param plugin The {@link Plugin} to unregister.
      */
     public static void removeExceptionHandler(final Plugin plugin) {
 
        for (final ExceptionHandler exceptionHandler : exceptionHandlers) {
             if (exceptionHandler.getPlugin().equals(plugin)) {
                 exceptionHandlers.remove(plugin);
             }
         }
 
         throw new IllegalStateException("ExceptionHandler for plugin " + plugin.getName() + " not set");
     }
 
     /**
      * Handles an {@link GameException} in the correct {@link ExceptionHandler}.
      * 
      * @param exception The {@link GameException} to handle.
      */
     public static void exception(final GameException exception) {
 
        for (final ExceptionHandler exceptionHandler : exceptionHandlers) {
             if (exceptionHandler.getPlugin().equals(exception.getPlugin())) {
                 exceptionHandler.handle(exception);
                 return;
             }
         }
 
         exception.getPlugin().getLogger().warning("No exception handler set (can't catch " + exception + ")");
     }
 
     private ExceptionManager() {
 
     }
 
 }
