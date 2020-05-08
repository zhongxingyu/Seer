 package de.hpi.fgis.twitter;
 
 import static org.junit.Assert.assertEquals;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.TimeZone;
 
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import com.mongodb.DBObject;
 import com.mongodb.util.JSON;
 
 public class TweetObjectParserTest {
 	private static SimpleDateFormat DATE_FORMAT;
 	@BeforeClass
 	public static void setUpBeforeClass() throws Exception {
 		DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
 		DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
 	}
 
 	@Test
 	public void testTransform() throws ParseException {
 		TweetObjectParser parser = new TweetObjectParser();
 		DBObject tweet = (DBObject) JSON.parse("{ 'created_at': 'Fri Jan 25 18:09:29 +0000 2013', 'id': 294869617401282560, 'id_str': '294869617401282561', 'text': '???????? ???? - ???????? ??????????! ?????????????, ? ???? ??? ???????! http://t.co/JnTkBSVC #iPad #iPa dGames #GameInsight', 'source': '<a href=\"http://www.game-insight.com/\" rel=\"nofollow\">Mirrors of Albion</a>', 'truncated': false, 'in_reply_to_status_id': null, 'in_reply_to_status_id_str': null, 'in_reply_to_user_id': null, 'in_reply_to_user_id_str': null, 'in_reply_to_screen_name': null, 'user': { 'id': 477459620, 'id_str': '477459620', 'name': '???? ???????', 'screen_name': 'yarem27', 'location': '', 'url': null, 'description': null, 'protected': false, 'followers_count': 0, 'friends_count': 14, 'listed_count': 0, 'created_at': 'Sun Jan 29 06:35:11 +0000 2012', 'favourites_count': 0, 'utc_offset': null, 'time_zone': null, 'geo_enabled': true, 'verified': false, 'statuses_count': 118, 'lang': 'ru', 'contributors_enabled': false, 'is_translator': false, 'profile_background_color': 'C0DEED', 'profile_background_image_url': 'http://a0.twimg.com/images/themes/theme1/bg.png', 'profile_background_image_url_https': 'https://si0.twimg.com/images/themes/theme1/bg.png', 'profile_background_tile': false, 'profile_image_url': 'http://a0.twimg.com/profile_images/1796298287/image_normal.jpg', 'profile_image_url_https': 'https://si0.twimg.com/profile_images/1796298287/image_normal.jpg', 'profile_link_color': '0084B4', 'profile_sidebar_border_color': 'C0DEED', 'profile_sidebar_fill_color': 'DDEEF6', 'profile_text_color': '333333', 'profile_use_background_image': true, 'default_profile': true, 'default_profile_image': false, 'following': null, 'follow_request_sent': null, 'notifications': null }, 'geo': null, 'coordinates': null, 'place': null, 'contributors': null, 'retweet_count': 0, 'entities': { 'hashtags': [ { 'text': 'iPad', 'indices': [ 93, 98 ] }, { 'text': 'iPadGames', 'indices': [ 99, 109 ] }, { 'text': 'GameInsight', 'indices': [ 110, 122 ] } ], 'urls': [ { 'url': 'http://t.co/JnTkBSVC', 'expanded_url': 'http://gigam.es/tw_psAlbion', 'display_url': 'gigam.es/tw_psAlbion', 'indices': [ 72, 92 ] } ], 'user_mentions': [] }, 'favorited': false, 'retweeted': false, 'possibly_sensitive': false, 'lang': 'ru' }");
 
		DBObject expected = (DBObject) JSON.parse("{ 'created_at': null, 'tweet_id': 294869617401282560, 'text': '???????? ???? - ???????? ??????????! ?????????????, ? ???? ??? ???????! http://t.co/JnTkBSVC #iPad #iPa dGames #GameInsight', 'user_id' : 477459620, 'retweeted': false, 'lang': 'ru', 'urls': [ 'http://gigam.es/tw_psAlbion' ], 'hashtags': [ 'iPad', 'iPadGames' , 'GameInsight' ] }");
 		expected.put("created_at", DATE_FORMAT.parseObject("2013-01-25T18:09:29"));
 
 		assertEquals(
 				expected,
 				parser.transform(tweet)
 				);
 	}
 
 }
