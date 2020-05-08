 package ru.yinfag.chitose;
 
 import org.jivesoftware.smack.PacketListener;
 import org.jivesoftware.smack.XMPPException;
 import org.jivesoftware.smack.packet.Message;
 import org.jivesoftware.smack.packet.Packet;
 import org.jivesoftware.smack.packet.PacketExtension;
 import org.jivesoftware.smack.packet.Presence;
 import org.jivesoftware.smackx.muc.MultiUserChat;
 import org.jivesoftware.smackx.packet.MUCUser;
 import java.net.*;
 import java.io.*;
 import java.net.URL;
 import java.util.HashSet;
 import java.util.Random;
 import java.util.Set;
 import java.util.concurrent.atomic.AtomicMarkableReference;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.util.*;
 import java.util.Properties;
 
 
 class ChitoseMUCListener implements PacketListener {
 	// регэкспы регэкспушки
 	private static final Pattern p = Pattern.compile("(?:(?:Chitose)|(?:[Чч]итосе)).*?кинь.*?(\\d+)[dд](\\d+)");
 	private static final Pattern p1 = Pattern.compile(".*?(?:(?:Chitose)|(?:[Чч]итосе)).*?(?:(?:запости)|(?:доставь)).*?(?:([\\w().*\\+]+?)|(няшку))\\.?$");
 	private static final Pattern p2 = Pattern.compile("sample_url=\"(.+?)\"");
 	private static final Pattern p3 = Pattern.compile("(?:(?:Chitose)|(?:[Чч]итосе)).*?расскажи.*?про \"([А-Яа-яA-Za-z]+?)\"");
 	private static final String p4 = "отсортировано по дате выхода";
 	private static final Pattern p5 = Pattern.compile("\\'estimation\\'\\>(.+?)\\&nbsp");
	private static final Pattern p6 = Pattern.compile("подробнее о перепечатке текстов\\<\\/a\\>\\<br\\>\\<br\\>\\<p align\\=justify class\\=\\'review\\'\\>(.+?)\\<\\/p\\>");
 	private static final String p7 = "<b>Раздел &laquo;анимация&raquo;";
 	private static final Pattern p8 = Pattern.compile("animation/animation.php\\?id=(\\d+)");
 	
 	private static final Set<String> VOICED_ROLES = new HashSet<>();
 
 	static {
 		VOICED_ROLES.add("participant");
 		VOICED_ROLES.add("moderator");
 	}
 
 
 	private final MultiUserChat muc;
 
 	// r - рандом
 	private final Random r = new Random();
 	
 	private final String conference;
 	private final String defaultNickname;
 
 	private AtomicMarkableReference<String> nick = new AtomicMarkableReference<>("Chitose", false);
 
 	ChitoseMUCListener(final MultiUserChat muc, final Properties props) {
 		this.muc = muc;
 		conference = props.getProperty("conference");
 		defaultNickname = props.getProperty("nickname");
 	}
 	
 
 	public PacketListener newProxypacketListener() {
 		return new PacketListener() {
 			@Override
 			public void processPacket(final Packet packet) {
 				ChitoseMUCListener.this.processPacket(packet);
 			}
 		};
 	}
 
 	@Override
 	public void processPacket(Packet packet) {
		System.out.println("Incoming packet: " + packet);
 		if (packet instanceof Presence) {
			System.out.println("presence");
 			processPresence((Presence) packet);
 		} else if (packet instanceof Message) {
			System.out.println("message");
 			processMessage((Message) packet);
 		}
 	}
 
 	private void processMessage(final Message message) {
 		//отвечаем в чятик на определённое слово
 		if ("*smooch*".equals(message.getBody())) {
 			try {
 				muc.sendMessage("*nosebleed*");
 			} catch (XMPPException e) {
 				e.printStackTrace();
 			}
 			return;
 		}
 		//отвечаем на определённую строку случайным ответом
 		if ("Янус - няша?".equals(message.getBody())) {
 			int rnum = r.nextInt(2);
 			if (rnum == 0) {
 				try {
 					muc.sendMessage("Няша конечно же. Учит джаве всех итц. ^^");
 				} catch (XMPPException e) {
 					e.printStackTrace();
 				}
 			} else if (rnum == 1) {
 				try {
 					muc.sendMessage("Янус - быдло ёбаное, и задрал итц всех своей джавой. ><");
 				} catch (XMPPException e) {
 					e.printStackTrace();
 				}
 			}
 			return;
 		}
 		//постим няшек
 		Matcher m1 = p1.matcher(message.getBody());
 		if (m1.matches()) {
 			URL gelbooru;
 			URLConnection c;
 			int rnum2 = r.nextInt(3);
 			if (rnum2 == 0 || rnum2 == 1) {
 				String nyakaName;
 				if ("няшку".equals(m1.group(2))) {
 					nyakaName = "";
 				} else {
 					nyakaName = m1.group(1);
 				}
 				try {
 					gelbooru = new URL("http://gelbooru.com/index.php?page=dapi&s=post&q=index&limit=10000&tags=solo+"+nyakaName);
 				} catch (MalformedURLException e) {
 					log("Не получилось составить урл для запроса в гелбуру", e);
 					return;
 				}
 				try {
 					c = gelbooru.openConnection();
 				} catch (IOException e) {
 					log("Не получилось открыть соединение с гелбуру", e);
 					return;
 				}
 				c.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.7 (KHTML, like Gecko) Chrome/16.0.912.77 Safari/535.7");
 				try (BufferedReader in = new BufferedReader(new InputStreamReader(c.getInputStream()))) {
 					String inputLine;
 					final List<String> urls = new ArrayList<String>();
 					while ((inputLine = in.readLine()) != null) {
 						Matcher m2 = p2.matcher(inputLine);
 						if (!m2.find()) {
 							continue;
 						}
 						final String postUrl = String.format(m2.group(1));
 						urls.add(postUrl);
 					}
 					if (urls.size() == 0) {
 						try {
 							muc.sendMessage(m1.group(1) +" не няшка!");
 						} catch (XMPPException e1) {
 								e1.printStackTrace();
 						}
 						return;
 					}
 					try {
 						Random random = new Random();
 						muc.sendMessage(urls.get(random.nextInt(urls.size())));
 					} catch (XMPPException e) {
 						e.printStackTrace();
 					}
 					
 				} catch (IOException e) {
 					 log("Ошибка ввода-вывода при чтении страницы", e);
 					 try {
 						 muc.sendMessage("Няшки закрыты на ремонт.");
 					 } catch (XMPPException e1) {
 						e1.printStackTrace();
 					}
 				}
 			}
 			if (rnum2 == 3) {
 				try {
 					muc.sendMessage("gelbooru.com - Все няшки там. Удачи, лентяй.");
 				} catch (XMPPException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 				
 		//тырим описание с вротарта
 		Matcher m3 = p3.matcher(message.getBody());
 		if (m3.matches()) {
 			URL worldart;
 			int rnum = r.nextInt(3);
 			if (rnum == 0 || rnum == 1 || rnum == 2) {
 				String title = m3.group(1);
 				try {
 					worldart = new URL("http://www.world-art.ru/search.php?public_search=" +title+"&global_sector=animation");
 				} catch (MalformedURLException e) {
 					log("Не получилось составить урл для запроса на вротарт", e);
 					return;
 				}
 				boolean multipleTitleVariants = false;
 				boolean animationPresent = false;
 				try (BufferedReader in = new BufferedReader(new InputStreamReader(worldart.openStream(), "cp1251"))) {
 					String inputLine;
 					while ((inputLine = in.readLine()) != null) {
 						if (inputLine.contains(p4)) {
 							multipleTitleVariants = true;
 						}
 						if (inputLine.contains(p7)) {
 							animationPresent = true;
 						}
 						if (multipleTitleVariants && animationPresent) {
 							break;
 						}
 					}
 				} catch (IOException e) {
 					log("Ошибка ввода-вывода при чтении страницы", e);
 					try {
 						muc.sendMessage("Няшки закрыты на ремонт.");
 					} catch (XMPPException e1) {
 						e1.printStackTrace();
 					}
 				}
 				System.out.format("MTV: %b; AP: %b%n", multipleTitleVariants, animationPresent);
 				if (multipleTitleVariants) {
 					try (BufferedReader in = new BufferedReader(new InputStreamReader(worldart.openStream(), "cp1251"))) {
 						final List<String> titles = new ArrayList<String>();
 						String inputLine;
 						while ((inputLine = in.readLine()) != null) {
 							Matcher m5 = p5.matcher(inputLine);
 							while (m5.find()) {
 								final String postTitles = m5.group(1);
 								titles.add(postTitles);
 							}
 						}
 						String titleList = Utils.join(titles, ", ");
 						try {
 							muc.sendMessage("Возможно ты имел ввиду: "+titleList);
 						} catch (XMPPException e) {
 							e.printStackTrace();
 						}
 					} catch (IOException e) {
 						log("Ошибка ввода-вывода при чтении страницы", e);
 						try {
 							muc.sendMessage("Няшки закрыты на ремонт.");
 						} catch (XMPPException e1) {
 							e1.printStackTrace();
 						}
 					}
 				} else if (animationPresent) {
 					try (BufferedReader in = new BufferedReader(new InputStreamReader(worldart.openStream(), "cp1251"))) {
 						String inputLine;
 						String titlelink = "";
						URL worldart1;
 						while ((inputLine = in.readLine()) != null) {
 							Matcher m8 = p8.matcher(inputLine);
 							if (!m8.find()) {
 								continue;
 							}
 							titlelink = m8.group(1);
 						}
 						try {
 							worldart1 = new URL("http://www.world-art.ru/animation/animation.php?id="+titlelink);
 						} catch (MalformedURLException e) {
 							log("Не получилось составить урл для запроса на вротарт", e);
 							return;
 						}
 						String synopsis1 = "Нет такого мультфильма!";
						try (BufferedReader in1 = new BufferedReader(new InputStreamReader(worldart1.openStream(),"cp1251" ))) {
 							String inputLine1;
 							while ((inputLine1 = in1.readLine()) != null) {
 								Matcher m6 = p6.matcher(inputLine1);
 								if (!m6.find()) {
 									continue;
 								}
 								synopsis1 = m6.group(1);
 							}
 						}
 						try {
 							muc.sendMessage(synopsis1);
 						} catch (XMPPException e1) {
 							e1.printStackTrace();
 						}
 					} catch (IOException e) {
 						log("Ошибка ввода-вывода при чтении страницы", e);
 						try {
 							muc.sendMessage("Няшки закрыты на ремонт.");
 						} catch (XMPPException e1) {
 							e1.printStackTrace();
 						}
 					}
 				} else {
 					try (BufferedReader in = new BufferedReader(new InputStreamReader(worldart.openStream(), "cp1251"))) {
 						String inputLine;
 						String synopsis = "Нет такого мультфильма!";
 						while ((inputLine = in.readLine()) != null) {
 							Matcher m6 = p6.matcher(inputLine);
 							if (!m6.find()) {
 								continue;
 							}
 							synopsis = m6.group(1);
 						}
 						try {
 							muc.sendMessage(synopsis);
 						} catch (XMPPException e1) {
 							e1.printStackTrace();
 						}
 					} catch (IOException e) {
 						log("Ошибка ввода-вывода при чтении страницы", e);
 						try {
 							muc.sendMessage("Няшки закрыты на ремонт.");
 						} catch (XMPPException e1) {
 							e1.printStackTrace();
 						}
 					} 
 				}
 			}
 		}
 	
 		//бросаем костяшки
 		Matcher m = p.matcher(message.getBody());
 		if (m.matches()) {
 			int rnum1 = r.nextInt(2);
 			if (rnum1 == 0) {
 				int a = Integer.parseInt(m.group(1));
 				int b = Integer.parseInt(m.group(2));
 				if (a > 9999 || b > 99999) {
 					try {
 						muc.sendMessage("Я не настолько умная же! ><'");
 					} catch (XMPPException e) {
 						e.printStackTrace();
 					}
 					return;
 				}
 				int result = 0;
 				for (int i = 0; i < a; i++) {
 					result += r.nextInt(b) + 1;
 				}
 				try {
 					muc.sendMessage("Выпало " + result + ", такие дела, нян!");
 				} catch (XMPPException e) {
 					e.printStackTrace();
 				}
 			}
 			if (rnum1 == 1) {
 				try {
 					muc.sendMessage("Неохота. ~___~");
 				} catch (XMPPException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 
 	private void processPresence(final Presence presence) {
 		System.out.println(presence.getFrom());
 		if ((conference + "/" + nick.getReference()).equals(presence.getFrom())) {
 			for (PacketExtension extension : presence.getExtensions()) {
 				System.out.println("e: " + extension);
 				if (extension instanceof MUCUser) {
 					final MUCUser.Item item = ((MUCUser) extension).getItem();
 					final String role = item.getRole();
 					if (!VOICED_ROLES.contains(role) && !nick.isMarked()) {
 						try {
 							final String newNick = "Дайте_войс";
 							muc.changeNickname(newNick);
 							nick.set(newNick, true);
 						} catch (XMPPException e) {
 							log("Failed to change nick", e);
 						}
 					} else if (VOICED_ROLES.contains(role) && nick.isMarked()) {
 						try {
 							final String newNick = defaultNickname;
 							muc.changeNickname(newNick);
 							nick.set(newNick, false);
 						} catch (XMPPException e) {
 							log("Failed to change nick", e);
 						}
 						try {
 							muc.sendMessage("Аригато!");
 						} catch (XMPPException e) {
 							log("Failed to say thanks", e);
 						}
 					}
 				}
 			}
 		}
 	}
 
 	//сокращаем всякие ебанутые принтлны и стектрейсы
 	private static void log(final String message, final Exception e) {
 		System.out.println(message);
 		if (e != null) {
 			e.printStackTrace();
 		}
 	}
 }
