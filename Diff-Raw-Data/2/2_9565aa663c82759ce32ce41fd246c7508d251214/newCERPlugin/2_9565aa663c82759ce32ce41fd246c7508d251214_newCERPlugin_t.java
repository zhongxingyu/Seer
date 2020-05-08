 //
 // newCERPlugin.java
 // darkcircle dot 0426 at gmail dot com
 //
 // This source can be distributed under the terms of GNU General Public License version 3
 // which is derived from the license of Manalith bot.
 package org.manalith.ircbot.plugin.newCER;
 
 import org.manalith.ircbot.ManalithBot;
 import org.manalith.ircbot.plugin.AbstractBotPlugin;
 import org.manalith.ircbot.resources.MessageEvent;
 
 public class newCERPlugin extends AbstractBotPlugin {
 
 
 	public newCERPlugin(ManalithBot bot) {
 		super(bot);
 		// TODO Auto-generated constructor stub
 	}
 
 	/* (non-Javadoc)
 	 * @see org.manalith.ircbot.plugin.IBotPlugin#getName()
 	 */
 	public String getName() {
 		// TODO Auto-generated method stub
 		return "환율 계산기";
 		//return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.manalith.ircbot.plugin.IBotPlugin#getNamespace()
 	 */
 	public String getNamespace() {
 		// TODO Auto-generated method stub
		return "curex";
 	}
 
 	/* (non-Javadoc)
 	 * @see org.manalith.ircbot.plugin.IBotPlugin#getHelp()
 	 */
 	public String getHelp() {
 		/*
 		String result = "!curex ( [Option] [Currency_Unit] [FieldAbbr/Amount] ) [Option] : show, lastround, convfrom, convto, buycash, cellcash, sendremit, recvremit ";
 		result += "[Currency_Unit] : USD:U.S, EUR:Europe, JPY:Japan, CNY:China, HKD:HongKong, TWD:Taiwan, GBP:Great Britain, CAD:Canada, CHF:Switzerland, SEK:Sweden, ";
 		result += "AUD:Australia, NZD:NewZealand, ISL:Israel, DKK:Denmark, NOK:Norway, SAR:Saudi Arabia, KWD:Kuweit, BHD:Bahrain, AED:United of Arab Emirates, ";
 		result += "JOD:Jordan, EGP:Egypt, THB:Thailand, SGD:Singapore, MYR:Malaysia, IDR:Indonesia, BND:Brunei, INR:India, PKR:Pakistan, BDT:Bangladesh, PHP:Philippine, ";
 		result += "MXN:Mexico, BRL:Brazil, VND:Vietnam, ZAR:Republic of South Africa, RUB:Russia, HUF:Hungary, PLN:Poland ";
 		result += "[FieldAbbr] (show 명령에만 해당) 모두보기, 매매기준, 현찰매수, 현찰매도, 송금보냄, 송금받음, 환수수료, 대미환산 ";
 		result += "[Amount] 금액";
 		*/
 		return "(null)";
 	}
 
 	/* (non-Javadoc)
 	 * @see org.manalith.ircbot.plugin.IBotPlugin#onJoin(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
 	 */
 	public void onJoin(String channel, String sender, String login,
 			String hostname) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/* (non-Javadoc)
 	 * @see org.manalith.ircbot.plugin.IBotPlugin#onMessage(org.manalith.ircbot.resources.MessageEvent)
 	 */
 	public void onMessage(MessageEvent event) {
 		// TODO Auto-generated method stub
 		String msg = event.getMessage();
 		String channel = event.getChannel();
 		
 		String [] command = msg.split("\\s");
 		if ( command[0].substring(0,6).equals("!curex") )
 		{
 			String [] subcmd = command[0].split("\\:");
 			if ( !subcmd[0].equals("!curex") ) return;
 			else
 			{
 				if ( subcmd.length == 1 )
 				{
 					String mergedcmd = "";
 					for ( int i = 1; i < command.length ; i++ )
 					{
 						mergedcmd += command[i];
 						if ( i != command.length - 1 ) mergedcmd += " ";
 					}
 					
 					try 
 					{
 						newCERRunner runner = new newCERRunner ( event.getSender(), this.getResourcePath(), mergedcmd );
 						
 						String result = runner.run();
 						if ( result.equals("Help!") )
 						{
 							bot.sendLoggedMessage(channel, CERInfoProvider.getIRCHelpMessagePart1());
 							bot.sendLoggedMessage(channel, CERInfoProvider.getIRCHelpMessagePart2());
 							bot.sendLoggedMessage(channel, CERInfoProvider.getIRCHelpMessagePart3());
 							bot.sendLoggedMessage(channel, CERInfoProvider.getIRCHelpMessagePart4());
 						}
 						else
 						{
 							bot.sendLoggedMessage(channel, result);
 						}
 					}
 					catch( Exception e )
 					{
 						bot.sendLoggedMessage(channel, e.getMessage());
 					}
 				}
 				else if ( subcmd.length > 2) return;
 				else
 				{
 					// remerge strings separated by space.
 					String userNick = event.getSender();
 					
 					String arg = "";
 					for ( int i = 1; i < command.length ; i++ )	
 					{
 						if ( command[i].equals(" ") ) continue;
 						arg += command[i];
 					}
 						
 					
 					newCERCustomSettingManager csMan = new newCERCustomSettingManager ( this.getResourcePath(), channel, userNick, arg );
 					
 					if ( subcmd[1].equals("sub") )
 						bot.sendLoggedMessage(channel, csMan.addUserSetting() );
 					else if ( subcmd[1].equals("unsub") )
 						bot.sendLoggedMessage(channel, csMan.removeUserSetting() );
 					else 	return;
 				}
 			}
 		}
 	}
 }
