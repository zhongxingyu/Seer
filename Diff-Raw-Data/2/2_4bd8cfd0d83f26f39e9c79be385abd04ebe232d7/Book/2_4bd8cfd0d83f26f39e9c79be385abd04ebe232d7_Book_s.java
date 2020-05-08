 package pppp;
 
 import com.example.base.BaseClass;
 import aaa;
 
 /**
  * book 【書籍マスタ】
  * テーブルの説明<br>
  * あいうえお<br>
  * かきくけこ<br>
  * 改行込みだよ
  *
  * @author test
  */
 public class Book extends BaseClass {
 /* SQL
 SELECT book_id,book_name,price,book_type,insert_datetime,update_datetime
 FROM book;
 
 INSERT INTO book(book_id,book_name,price,book_type,insert_datetime,update_datetime)
 VALUES(?,?,?,?,?,?);
 
 UPDATE book
 SET book_id=?,book_name=?,price=?,book_type=?,insert_datetime=?,update_datetime=?;
 */
 
 	private Integer BookId;
 	private String BookName;
 	private Integer Price;
 	private String BookType;
 	private Timestamp InsertDatetime;
 	private Timestamp UpdateDatetime;
 
 	/**
 	 * book_id 【書籍ID】の取得<br>
 	 * 【型】 INTEGER
 	 * 
 	 * @return BookId 【書籍ID】
 	 */
 	public Integer getBookId() {
 		return BookId;
 	}
 
 	/**
 	 * book_id 【書籍ID】のセット<br>
 	 * 【型】 INTEGER
 	 * 
 	 * @param BookId
 	 *             【書籍ID】
 	 */
 	public void setBookId(Integer BookId) {
 		this.BookId = BookId;
 	}
 
 	/**
 	 * book_name 【書名】の取得<br>
 	 * 【型】 VARCHAR(80)
 	 * 
 	 * @return BookName 【書名】
 	 */
 	public String getBookName() {
 		return BookName;
 	}
 
 	/**
 	 * book_name 【書名】のセット<br>
 	 * 【型】 VARCHAR(80)
 	 * 
 	 * @param BookName
 	 *             【書名】
 	 */
 	public void setBookName(String BookName) {
 		this.BookName = BookName;
 	}
 
 	/**
 	 * price 【価格】の取得<br>
 	 * 【型】 INTEGER
 	 * 
 	 * @return Price 【価格】
 	 */
 	public Integer getPrice() {
 		return Price;
 	}
 
 	/**
 	 * price 【価格】のセット<br>
 	 * 【型】 INTEGER
 	 * 
 	 * @param Price
 	 *             【価格】
 	 */
 	public void setPrice(Integer Price) {
 		this.Price = Price;
 	}
 
 	/**
 	 * book_type 【書籍種別】の取得<br>
 	 * 【型】 CHAR(1)
 	 *
 	 * 1: 小説<br>
 	 * 2: コミック<br>
 	 * 3: 実用書
 	 * 
 	 * @return BookType 【書籍種別】
 	 */
 	public String getBookType() {
 		return BookType;
 	}
 
 	/**
 	 * book_type 【書籍種別】のセット<br>
 	 * 【型】 CHAR(1)
 	 *
 	 * 1: 小説<br>
 	 * 2: コミック<br>
 	 * 3: 実用書
 	 * 
 	 * @param BookType
 	 *             【書籍種別】
 	 */
 	public void setBookType(String BookType) {
 		this.BookType = BookType;
 	}
 
 	/**
 	 * insert_datetime 【登録日時】の取得<br>
 	 * 【型】 TIMESTAMP WITH TIME ZONE
 	 * 
 	 * @return InsertDatetime 【登録日時】
 	 */
 	public Timestamp getInsertDatetime() {
 		return InsertDatetime;
 	}
 
 	/**
 	 * insert_datetime 【登録日時】のセット<br>
 	 * 【型】 TIMESTAMP WITH TIME ZONE
 	 * 
 	 * @param InsertDatetime
 	 *             【登録日時】
 	 */
 	public void setInsertDatetime(Timestamp InsertDatetime) {
 		this.InsertDatetime = InsertDatetime;
 	}
 
 	/**
 	 * update_datetime 【更新登録日時】の取得<br>
 	 * 【型】 TIMESTAMP
 	 * 
 	 * @return UpdateDatetime 【更新登録日時】
 	 */
 	public Timestamp getUpdateDatetime() {
 		return UpdateDatetime;
 	}
 
 	/**
 	 * update_datetime 【更新登録日時】のセット<br>
 	 * 【型】 TIMESTAMP
 	 * 
 	 * @param UpdateDatetime
 	 *             【更新登録日時】
 	 */
 	public void setUpdateDatetime(Timestamp UpdateDatetime) {
 		this.UpdateDatetime = UpdateDatetime;
 	}
 
 }
