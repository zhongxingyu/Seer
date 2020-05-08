 package ru.dark32.chat.ichanels;
 
 import org.bukkit.Instrument;
 import org.bukkit.Material;
 import org.bukkit.Note;
 
 import ru.dark32.chat.Main;
 import ru.dark32.chat.chanels.BaseChanel;
 import ru.dark32.chat.chanels.BroadChanel;
 import ru.dark32.chat.chanels.ChanceChanel;
 import ru.dark32.chat.chanels.ChanelRegister;
 import ru.dark32.chat.chanels.ItemChanel;
 import ru.dark32.chat.chanels.PersonalMessageChanel;
 import ru.dark32.chat.chanels.RangeChanel;
 import ru.dark32.chat.chanels.RangeItemChanel;
 import ru.dark32.chat.chanels.RangeRequisiteItemChanel;
 
 /**
  * @author Andrew
  * 
  */
 public enum ETypeChanel {
 		BASE {
 			@Override
 			public IChanel setChanel(final String name ) {
 				IChanel chanel = new BaseChanel();
 				ETypeChanel.setBase(chanel, name);
 				return chanel;
 
 			}
 		},
 		RANGE {
 			@Override
 			public IChanel setChanel(final String name ) {
 				IChanel chanel = new RangeChanel();
 				ETypeChanel.setBase(chanel, name);
 				((IRangeChanel) chanel).setRange(Main.config.getInt("Chat." + name + ".range", 200));
 				return chanel;
 			}
 		},
 		ITEM {
 			@Override
 			public IChanel setChanel(final String name ) {
 				IChanel chanel = new ItemChanel();
 				ETypeChanel.setBase(chanel, name);
 				ETypeChanel.setItem(chanel, name);
 				return chanel;
 			}
 		},
 		PM {
 			@Override
 			public IChanel setChanel(final String name ) {
 				IChanel chanel = new PersonalMessageChanel();
 				ETypeChanel.setBase(chanel, name);
 				((IPersonalMessagesChanel) chanel).setFormatTo(Main.config.getString("Chat." + name + ".formatTo",
 						"Chat." + name + ".formatTo"));
 				((IPersonalMessagesChanel) chanel).setFormatFrom(Main.config.getString("Chat." + name + ".formatFrom",
 						"Chat." + name + ".formatFrom"));
 				((IPersonalMessagesChanel) chanel).setFormatSpy(Main.config.getString("Chat." + name + ".formatSpy",
 						"Chat." + name + ".formatSpy"));
 				((IPersonalMessagesChanel) chanel).setPmSearchNickMode(Main.config.getInt("Chat." + name
 						+ ".PMSearchNickMode", 0));
 				return chanel;
 			}
 		},
 		RANGE_ITEM {
 			@Override
 			public IChanel setChanel(final String name ) {
 				IChanel chanel = new RangeItemChanel();
 				ETypeChanel.setBase(chanel, name);
 				ETypeChanel.setItem(chanel, name);
 				((IRangeChanel) chanel).setRange(Main.config.getInt("Chat." + name + ".range"));
 
 				return chanel;
 			}
 		},
 		REQUISITE {
 			@SuppressWarnings("deprecation" )
 			@Override
 			public IChanel setChanel(final String name ) {
 				IChanel chanel = new RangeRequisiteItemChanel();
 				ETypeChanel.setBase(chanel, name);
 				ETypeChanel.setItem(chanel, name);
 				((IRangeChanel) chanel).setRange(Main.config.getInt("Chat." + name + ".range"));
 				((IRangeRequisiteItemChanel) chanel).setRequiseteItemId(Main.config.getInt("Chat." + name
 						+ ".requisete.id", ((IItemChanel) chanel).getItemId()));
 				((IRangeRequisiteItemChanel) chanel).setRequiseteItemSubId(Main.config.getInt("Chat." + name
 						+ ".requisete.subid", ((IItemChanel) chanel).getItemSubId()));
 				((IRangeRequisiteItemChanel) chanel).setRequiseteItemAmount(Main.config.getInt("Chat." + name
 						+ ".requisete.amount", 0));
 				((IRangeRequisiteItemChanel) chanel).setRequiseteItemMaterial(Material.getMaterial(Main.config
 						.getString("Chat." + name + ".requisete.material", ((IItemChanel) chanel).getItemMaterial()
 								.name())));
 				return chanel;
 			}
 		},
 		CHANCE {
 
 			@Override
 			public IChanel setChanel(final String name ) {
 				IChanel chanel = new ChanceChanel();
 				ETypeChanel.setBase(chanel, name);
 				((IChanceChanel) chanel).setRange(Main.config.getInt("Chat." + name + ".range", 200));
 				((IChanceChanel) chanel).setChance(Main.config.getInt("Chat." + name + ".chance", 50),
 						Main.config.getInt("Chat." + name + ".min", 5));
 				((IChanceChanel) chanel).setLuckUnLuck(
 						Main.config.getString("Chat." + name + ".luck", "Chat." + name + ".luck"),
 						Main.config.getString("Chat." + name + ".unluck", "Chat." + name + ".unluck"));
 				((IChanceChanel) chanel).setFormatRoll(Main.config.getString("Chat." + name + ".formatroll", "Chat."
 						+ name + ".formatroll"));
 				((IChanceChanel) chanel).setFormatLuck(Main.config.getString("Chat." + name + ".formatLuck", "Chat."
 						+ name + ".formatLuck"));
 				return chanel;
 			}
 		},
 		BROAD {
 
 			@Override
 			public IChanel setChanel(final String name ) {
 				IChanel chanel = new BroadChanel();
 				ETypeChanel.setBase(chanel, name);
 				((IBroadChanel) chanel).setPattern(Main.config.getStringList("Chat." + name + ".pattern"));
 				return chanel;
 			}
 		},
 		NONE {
 			@Override
 			public IChanel setChanel(final String name ) {
 				return null;
 			}
 		};
 	public static ETypeChanel get(final String type ) {
 		if (type.equalsIgnoreCase("BASE")) {
 			return BASE;
 		}
 		if (type.equalsIgnoreCase("RANGE")) {
 			return RANGE;
 		}
 		if (type.equalsIgnoreCase("ITEM")) {
 			return ITEM;
 		}
 		if (type.equalsIgnoreCase("PM")) {
 			return PM;
 		}
 		if (type.equalsIgnoreCase("RANGE_ITEM")) {
 			return RANGE_ITEM;
 		}
 		if (type.equalsIgnoreCase("REQUISITE")) {
 			return REQUISITE;
 		}
 		if (type.equalsIgnoreCase("CHANCE")) {
 			return CHANCE;
 		}
 		if (type.equalsIgnoreCase("BROAD")) {
 			return BROAD;
 		}
 		if (type.equalsIgnoreCase("NONE")) {
 			return NONE;
 		}
 		return NONE;
 	}
 
 	/**
 	 * 
 	 */
 	public abstract IChanel setChanel(final String name );
 
 	private static IChanel setBase(final IChanel chanel, final String name ) {
 		chanel.setIndex(ChanelRegister.getNextIndex());
 		chanel.setName(Main.config.getString("Chat." + name + ".name", "Chat." + name + ".name"));
 		chanel.setFormat(Main.config.getString("Chat." + name + ".format", "Chat." + name + ".format"));
 		chanel.setEnable(Main.config.getBoolean("Chat." + name + ".enable", false));
 		chanel.setWorldChat(Main.config.getBoolean("Chat." + name + ".world", false));
 		chanel.setTabes(Main.config.getBoolean("Chat." + name + ".tab", true));
 		chanel.setPrefix(Main.config.getString("Chat." + name + ".prefix", "Chat." + name + ".prefix"));
 		chanel.setSign(Main.config.getString("Chat." + name + ".sign", "Chat." + name + ".sign").charAt(0));
 		chanel.setListenerMessage(
 				Main.config.getString("Chat." + name + ".listenerMessage", "Chat." + name + ".listenerMessage"),
 				Main.config.getString("Chat." + name + ".noListenerMessage", "Chat." + name + ".noListenerMessage"),
 				Main.config.getBoolean("Chat." + name + ".isListenerMessage", false));
 		chanel.setNeedPerm(Main.config.getBoolean("Chat." + name + ".needPerm", false));
		chanel.setPimk(
				Main.config.getBoolean("Chat." + name + ".pimk.enable", false),
				Instrument.valueOf(Main.config.getString("Chat." + name + ".pimk.instrument", "PIANO")),
				new Note(Main.config.getInt("Chat." + name + ".pimk.note.octava", 1), 
						Note.Tone.valueOf(Main.config.getString("Chat." + name + ".pimk.note.tone", "F")),
						Main.config.getBoolean("Chat." + name+ ".pimk.tone.sharped", false)));
 		chanel.setInnerName(name);
 		return chanel;
 	}
 
 	@SuppressWarnings("deprecation" )
 	private static IChanel setItem(final IChanel chanel, final String name ) {
 		((IItemChanel) chanel).setItemId(Main.config.getInt("Chat." + name + ".item.id", 0));
 		((IItemChanel) chanel).setItemSubId(Main.config.getInt("Chat." + name + ".item.subid", 0));
 		((IItemChanel) chanel).setItemAmount(Main.config.getInt("Chat." + name + ".item.amount", 1));
 		((IItemChanel) chanel).setItemMaterial(Material.getMaterial(Main.config.getString("Chat." + name
 				+ ".item.material", "AIR")));
 		((IItemChanel) chanel).setRequestPprefix(Main.config.getBoolean("Chat." + name + ".requestPrefix", true));
 
 		return chanel;
 	}
 }
