 package com.drtshock.willie.command.fun;
 
 import org.pircbotx.Channel;
 import org.pircbotx.User;
 
 import com.drtshock.willie.Willie;
 import com.drtshock.willie.command.CommandHandler;
 
public class BotSnacksCommandHandler implements CommandHandler {
 
    @Override
    public void handle(Willie bot, Channel channel, User sender, String[] args) {
        bot.sendAction(channel, "NOM NOM NOM");
    }
 }
