 
 package com.quartercode.quarterbukkit.api.exception;
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.plugin.Plugin;
 import com.quartercode.quarterbukkit.api.Updater;
 
 /**
  * The InstallException occurres if something goes wrong while installing a plugin via {@link Updater}.
  */
 public class InstallException extends GameException {
 
     private final Updater       updater;
     private final Exception     cause;
     private final CommandSender causer;
 
     /**
     * Creates a InstallException filled with the causing {@link Updater} and a cause as an {@link Exception} for the error.
      * 
      * @param plugin The causing {@link Plugin}.
      * @param updater The {@link Updater} which caused the error.
      * @param cause The cause for the error.
      */
     public InstallException(final Plugin plugin, final Updater updater, final Exception cause) {
 
         super(plugin);
         this.updater = updater;
         this.cause = cause;
         causer = null;
     }
 
     /**
     * Creates a InstallException filled with the causing {@link Updater}, a cause as an {@link Exception} for the error and the {@link CommandSender} who executed the updater.
      * 
      * @param plugin The causing {@link Plugin}.
      * @param updater The {@link Updater} which caused the error.
      * @param cause The cause for the error.
      * @param causer The {@link CommandSender} who caused the exception.
      */
     public InstallException(final Plugin plugin, final Updater updater, final Exception cause, final CommandSender causer) {
 
         super(plugin);
         this.updater = updater;
         this.cause = cause;
         this.causer = causer;
     }
 
     /**
     * Creates a InstallException filled with the causing {@link Updater}, a cause as an {@link Exception} for the error and an informational message.
      * 
      * @param plugin The causing {@link Plugin}.
      * @param updater The {@link Updater} which caused the error.
      * @param cause The cause for the error.
      * @param message The informational message.
      */
     public InstallException(final Plugin plugin, final Updater updater, final Exception cause, final String message) {
 
         super(plugin, message);
         this.updater = updater;
         this.cause = cause;
         causer = null;
     }
 
     /**
     * Creates a InstallException filled with the causing {@link Updater}, a cause as an {@link Exception} for the error, the {@link CommandSender} who executed the updater an an informational
      * message.
      * 
      * @param plugin The causing {@link Plugin}.
      * @param updater The {@link Updater} which caused the error.
      * @param cause The cause for the error.
      * @param causer The {@link CommandSender} who caused the exception.
      * @param message The informational message.
      */
     public InstallException(final Plugin plugin, final Updater updater, final Exception cause, final CommandSender causer, final String message) {
 
         super(plugin, message);
         this.updater = updater;
         this.cause = cause;
         this.causer = causer;
     }
 
     /**
      * Returns the {@link Updater} which caused the error.
      * 
      * @return The {@link Updater} which caused the error.
      */
     public Updater getUpdater() {
 
         return updater;
     }
 
     /**
      * Returns the cause for the error.
      * 
      * @return The cause for the error as an {@link Exception}.
      */
     public Exception getCause() {
 
         return cause;
     }
 
     /**
      * Returns the {@link CommandSender} causer.
      * 
      * @return The causer who hasn't enough permissions.
      */
     public CommandSender getCauser() {
 
         return causer;
     }
 
 }
