 package ru.yinfag.chitose;
 
 import java.sql.Connection;
 import java.util.List;
 import org.jivesoftware.smack.packet.Message;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.util.Properties;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 public class TokyotoshoMessageProcessor implements MessageProcessor {
 	
 	private final boolean enabled;
 	private final Pattern p1;
 	private final Pattern p2;
 	private final Pattern p3;
 	private final Statement statement;
 	
 	TokyotoshoMessageProcessor (
 			final Properties mucProps, 
 			final Connection dbconn
 	) throws SQLException {
 		enabled = "1".equals(mucProps.getProperty("Tokyotosho"));
 		String botname = mucProps.getProperty("nickname");
 		p1 = Pattern.compile(
 			".*?" + botname + ".* следи за релизами (.+?)"
 		);
 		p2 = Pattern.compile(
 			".*?" + botname + ".* удали фильтр (.+?)"
 		);
 		p3 = Pattern.compile(
 			".*?" + botname + ".* покажи фильтры"
 		);
 		statement = dbconn.createStatement();	
 	}
 	
 	@Override
 	public CharSequence process(final Message message) throws MessageProcessingException {
 		
 		if (!enabled) {
 			return null;
 		}
 		
 		final Matcher m1 = p1.matcher(message.getBody());	
 		final Matcher m2 = p2.matcher(message.getBody());
 		final Matcher m3 = p3.matcher(message.getBody());
 
 		if (m1.matches()){	
 			final String text = m1.group(1).replaceAll("'", "''").trim();
 			if (text.length() < 11) {	
 				try {
 					statement.execute("insert into filter (text) values ('" + text + "')");
 					return "Добавила фильтр "+text+" , ня!";
 				} catch (SQLException e) {
 					e.printStackTrace();
 					return "Не смогла добавить фильтр. т___т";
 				}
 			} else {
 				return "Слишком длинный фильтр.";
 			}			
 		} else if (m2.matches()){		
 			final List<String> filters = new ArrayList<>();
 			try (final ResultSet rs = statement.executeQuery("select text from filter")) {
 				while (rs.next()) {
 					filters.add(rs.getString("text"));
 				}
 			} catch (SQLException e) {
 				e.printStackTrace();
				return "Бд сломалась. т____т ";
 			}
 			final String text = m2.group(1).replaceAll("'", "''").trim();
 			if (filters.contains(text)) {
 				try {
 					statement.execute("DELETE FROM filter WHERE text='" + text + "'");
 					return "Фильтр " +text+ " удалён, ня!";
 				} catch (SQLException e) {
 					e.printStackTrace();
 					return "Не смогла удалить фильтр. т___т";
 				}
 			} else {
 				return "Нет такого фильтра же!";
 			}
 		} else if (m3.matches()) {
 			final List<String> filters = new ArrayList<>();
 			try (final ResultSet rs = statement.executeQuery("select text from filter")) {
 				while (rs.next()) {
 					filters.add(rs.getString("text"));
 				}
 			} catch (SQLException e) {
 				e.printStackTrace();
 				return "Не смогла получить список фильтров. т___т";
 			}
 			if (filters.isEmpty()) {
 				return "Фильтров нет.";
 			} else {
 				return Utils.join(filters, ", ");
 			}
 		} else {
 			return null;
 		}
 	}
 }
 			
 			
 		
 		
