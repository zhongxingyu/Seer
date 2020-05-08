 package net.gumbercules.loot.transaction;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 
 import net.gumbercules.loot.account.Account;
 import net.gumbercules.loot.backend.Database;
 import net.gumbercules.loot.repeat.RepeatSchedule;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.net.Uri;
 import android.util.Log;
 
 public class Transaction
 	implements Comparable<Transaction>
 {
 	public static final int DEPOSIT		= 0;
 	public static final int WITHDRAW	= 1;
 	public static final int CHECK		= 2;
 	
 	public static final int COMP_DATE	= 0;
 	public static final int COMP_AMT	= 1;
 	public static final int COMP_PARTY	= 2;
 	
 	private static int comp = -1;
 	
 	public static final String KEY_ID		= "t_id";
 	public static final String KEY_IDS		= "t_ids";
 	public static final String KEY_DATE		= "t_date";
 		
 	private int id;
 	public int account;
 	private boolean posted;
 	public boolean budget;
 	public int type;				// DEPOSIT, WITHDRAW, CHECK
 	public int check_num;
 	public Date date; 
 	public String party;
 	public double amount;
 	ArrayList<String> tags;
 	ArrayList<Uri> images;
 	
 	public Transaction()
 	{
 		this.id = -1;
 		this.account = -1;
 		this.posted = false;
 		this.budget = false;
 		this.date = null;
 		this.type = WITHDRAW;
 		this.amount = 0.00;
 		this.check_num = -1;
 		this.party = null;
 		this.tags = new ArrayList<String>();
 		this.images = new ArrayList<Uri>();
 	}
 	
 	public Transaction( boolean po, boolean b, Date d, int t, String pa, double a, int c )
 	{
 		this.id = -1;
 		this.account = -1;
 		this.posted = po;
 		this.budget = b;
 		this.date = d;
 		this.type = t;
 		this.amount = a;
 		this.check_num = c;
 		this.party = pa;
 		this.tags = new ArrayList<String>();
 		this.images = new ArrayList<Uri>();
 	}
 	
 	@SuppressWarnings("unchecked")
 	public Transaction( Transaction trans, boolean copy_id )
 	{
 		if (copy_id)
 			this.id = trans.id;
 		else
 			this.id = -1;
 		this.account = trans.account;
 		this.posted = trans.posted;
 		this.budget = trans.budget;
 		this.date = trans.date;
 		this.type = trans.type;
 		this.amount = trans.amount;
 		this.check_num = trans.check_num;
 		this.party = trans.party;
 		this.tags = (ArrayList<String>) trans.tags.clone();
 		this.images = (ArrayList<Uri>) trans.images.clone();
 	}
 	
 	public int addImage(Uri uri)
 	{
 		if (!this.images.contains(uri) && this.images.add(uri))
 		{
 			return 1;
 		}
 
 		return 0;
 	}
 	
 	public int addImages(Uri[] uris)
 	{
 		if (uris == null)
 		{
 			return 0;
 		}
 		
 		int i = 0;
 		
 		for (Uri uri : uris)
 		{
 			if (!this.images.contains(uri) && this.images.add(uri))
 			{
 				++i;
 			}
 		}
 		
 		return i;
 	}
 	
 	public int removeImage(Uri uri)
 	{
 		if (this.images.remove(uri))
 		{
 			return 1;
 		}
 		else
 		{
 			return 0;
 		}
 	}
 	
 	public int removeImages(Uri[] uris)
 	{
 		if (uris == null)
 		{
 			int size = this.images.size();
 			this.images.clear();
 			return size;
 		}
 		
 		int i = 0;
 		
 		for (Uri uri : uris)
 		{
 			if (this.images.remove(uri))
 			{
 				++i;
 			}
 		}
 		
 		return i;
 	}
 	
 	public int addTags( String tags )
 	{
 		String[] tagList = this.tagStringToList(tags);
 		if (tagList == null)
 			return 0;
 		
 		int i = 0;
 		for ( String tag : tagList )
 			if ( this.tags.add( tag ) )
 				++i;
 		return i;
 	}
 	
 	public int addTags( String[] tags )
 	{
 		if (tags == null)
 			return 0;
 		
 		int i = 0;
 		for ( String tag : tags )
 		{
 			if ( this.tags.add( tag ) )
 				++i;
 		}
 		return i;
 	}
 	
 	public int removeTags( String tags )
 	{
 		int i = 0;
 		for ( String tag : this.tagStringToList( tags ) )
 			while ( this.tags.remove(tag) )
 				++i;
 		return i;
 	}
 	
 	public int removeTags( String[] tags )
 	{
 		int i = 0;
 		for ( String tag : tags )
 			// remove all occurrences of tag
 			while ( this.tags.remove( tag ) )
 				++i;
 		return i;
 	}
 	
 	public String tagListToString()
 	{
 		if ( tags.size() == 0 )
 			return null;
 		
 		String tag_str = new String();
 		for ( String tag : tags )
 			tag_str += " " + tag;
 		
		return tag_str;
 	}
 	
 	public String[] getTagList()
 	{
 		String[] tagList = new String[tags.size()];
 		for ( int i = 0; i < tagList.length; ++i )
 			tagList[i] = tags.get(i);
 		
 		return tagList;
 	}
 	
 	private String[] tagStringToList( String tags )
 	{
 		if (tags == null)
 			return null;
 		
 		return tags.split(" ");
 	}
 	
 	public void setId(int id)
 	{
 		this.id = id;
 	}
 	
 	public int id()
 	{
 		return this.id;
 	}
 	
 	public int write( int account_num )
 	{
 		this.account = account_num;
 		if (this.id == -1)
 			return newTransaction();
 		else
 			return updateTransaction();
 	}
 	
 	private int newTransaction()
 	{
 		// invert the amount if it removed money from the account
 		double amount = this.amount;
 		int check = 0;
 		if ( this.type == Transaction.WITHDRAW )
 			amount = -amount;
 		else if ( this.type == Transaction.CHECK )
 		{
 			amount = -amount;
 			check = this.check_num;
 		}
 		
 		// insert the new row into the database
 		String insert = "insert into transactions (account,date,party,amount,check_num,budget,timestamp) " +
 						"values (?,?,?,?,?,?,strftime('%s','now'))";
 		Object[] bindArgs = {new Long(this.account), new Long(this.date.getTime()), this.party,
 				new Double(amount), new Long(check), new Long(Database.setBoolean(this.budget))};
 
 		SQLiteDatabase lootDB = Database.getDatabase();
 		lootDB.beginTransaction();
 		try
 		{
 			lootDB.execSQL(insert, bindArgs);
 		}
 		catch (SQLException e)
 		{
 			lootDB.endTransaction();
 			Log.e("Transaction.newTransaction", "error inserting row");
 			Log.e("Transaction.newTransaction", insert);
 			return -1;
 		}
 		
 		// get the id of that row
 		Cursor cur = lootDB.rawQuery("select max(id) from transactions", null);
 		if (!cur.moveToFirst())
 		{
 			cur.close();
 			lootDB.endTransaction();
 			Log.e("Transaction.newTransaction", "error selecting max(id)");
 			return -1;
 		}
 		
 		int id = cur.getInt(0);
 		cur.close();
 		this.id = id;
 		
 		// if tag writing is not successful, rollback the changes
 		if (writeTags())
 		{
 			if (writeImages())
 			{
 				lootDB.setTransactionSuccessful();
 			}
 			else
 			{
 				this.id = -1;
 				Log.e("Transaction.newTransaction", "error writing images");
 			}
 		}
 		else
 		{
 			this.id = -1;
 			Log.e("Transaction.newTransaction", "error writing tags");
 		}
 
 		lootDB.endTransaction();
 		
 		return this.id;
 	}
 	
 	private int updateTransaction()
 	{
 		String update = "update transactions set account = ?, date = ?, party = ?, amount = ?, " +
 						"check_num = ?, budget = ?, timestamp = strftime('%s','now') where id = ?";
 		double amount = this.amount;
 		int check = 0;
 		if ( this.type == Transaction.WITHDRAW )
 		{
 			amount = -amount;
 		}
 		else if ( this.type == Transaction.CHECK )
 		{
 			amount = -amount;
 			check = this.check_num;
 		}
 		
 		int acct_id = this.account;
 		if ( this.account == -1 )
 			acct_id = Account.getCurrentAccountNum();
 		Object[] bindArgs = {new Long(acct_id), new Long(this.date.getTime()), this.party,
 				new Double(amount), new Long(check), new Long(Database.setBoolean(this.budget)),
 				new Long(this.id)};
 		
 		SQLiteDatabase lootDB = Database.getDatabase();
 		try
 		{
 			lootDB.beginTransaction();
 			lootDB.execSQL(update, bindArgs);
 		}
 		catch (SQLException e)
 		{
 			lootDB.endTransaction();
 			return -1;
 		}
 		
 		// if tag writing is not successful, rollback the changes
 		int ret = -1;
 		if ((eraseTags() && writeTags() && (eraseImages() && writeImages())))
 		{
 			lootDB.setTransactionSuccessful();
 			ret = this.id;
 		}
 		lootDB.endTransaction();
 		
 		return ret;
 	}
 	
 	private boolean writeImages()
 	{
 		int size = this.images.size();
 		if (size == 0)
 		{
 			return true;
 		}
 		
 		String insert = "insert into images (trans_id,uri) values (?,?)";
 		Object[] bindArgs = new Object[2];
 		bindArgs[0] = new Long(this.id);
 		
 		SQLiteDatabase lootDb = Database.getDatabase();
 		lootDb.beginTransaction();
 		
 		try
 		{
 			for (int i = 0; i < size; ++i)
 			{
 				bindArgs[1] = this.images.get(i).toString();
 				lootDb.execSQL(insert, bindArgs);
 			}
 			
 			lootDb.setTransactionSuccessful();
 		}
 		catch (SQLException e)
 		{
 			return false;
 		}
 		finally
 		{
 			lootDb.endTransaction();
 		}
 		
 		return true;
 	}
 	
 	private boolean eraseImages()
 	{
 		SQLiteDatabase lootDb = Database.getDatabase();
 		String[] where = new String[] { Long.toString(this.id) };
 		
 		try
 		{
 			lootDb.delete("images", "trans_id = ?", where);
 		}
 		catch (SQLException e)
 		{
 			e.printStackTrace();
 			return false;
 		}
 		
 		return true;
 	}
 	
 	private boolean writeTags()
 	{
 		// no tags, nothing to write, it's a success
 		int sz = this.tags.size();
 		if (sz == 0)
 			return true;
 		
 		String insert = "insert into tags (trans_id,name) values (?,?)";
 		Object[] bindArgs = new Object[2];
 		bindArgs[0] = new Long(this.id);
 		
 		SQLiteDatabase lootDB = Database.getDatabase();
 		lootDB.beginTransaction();
 		
 		String[] used = new String[sz];
 		for (int x = 0; x < sz; ++x)
 			used[x] = "";
 		
 		try
 		{
 			int i = 0;
 			for ( String tag : this.tags )
 			{
 				// if the tag has already been used, go to the next one to avoid a sql constraint error
 				if (Arrays.binarySearch(used, tag) >= 0)
 					continue;
 				
 				bindArgs[1] = tag;
 				lootDB.execSQL(insert, bindArgs);
 				
 				used[i++] = tag;
 				Arrays.sort(used);
 			}
 			lootDB.setTransactionSuccessful();
 		}
 		catch (SQLException e)
 		{
 			Log.e("Transaction.writeTags", e.toString());
 			return false;
 		}
 		finally
 		{
 			lootDB.endTransaction();
 		}
 		
 		return true;
 	}
 	
 	private boolean eraseTags()
 	{
 		String del = "delete from tags where trans_id = ?";
 		Object[] bindArgs = {new Long(this.id)};
 		
 		SQLiteDatabase lootDB = Database.getDatabase();
 		lootDB.beginTransaction();
 		try
 		{
 			lootDB.execSQL(del, bindArgs);
 			lootDB.setTransactionSuccessful();
 		}
 		catch (SQLException e)
 		{
 			return false;
 		}
 		finally
 		{
 			lootDB.endTransaction();
 		}
 		
 		return true;
 	}
 		
 	public boolean post( boolean p )
 	{
 		String post = "update transactions set posted = ?, timestamp = strftime('%s','now'), " +
 					  "budget = 0 where id = ?";
 		Object[] bindArgs = {new Long(Database.setBoolean(p)), new Long(this.id)};
 		
 		SQLiteDatabase lootDB = Database.getDatabase();
 		lootDB.beginTransaction();
 		try
 		{
 			lootDB.execSQL(post, bindArgs);
 			lootDB.setTransactionSuccessful();
 		}
 		catch (SQLException e)
 		{
 			return false;
 		}
 		finally
 		{
 			lootDB.endTransaction();
 		}
 		
 		this.posted = p;
 		this.budget = false;
 		return true;
 	}
 	
 	public boolean isPosted()
 	{
 		return posted;
 	}
 	
 	public boolean erase()
 	{
 		return erase(true);
 	}
 	
 	public boolean erase(boolean erase_transfer)
 	{
 		int transfer = this.getTransferId();
 		if (erase_transfer && transfer != -1)
 		{
 			return this.eraseTransfer();
 		}
 		else
 		{
 			return this.eraseTransaction();
 		}
 	}
 	
 	private boolean eraseTransaction()
 	{
 		SQLiteDatabase lootDB = Database.getDatabase();
 		lootDB.beginTransaction();
 
 		String del = "delete from transactions where id = ?";
 		Object[] bindArgs = {new Long(this.id)};
 		
 		try
 		{
 			lootDB.execSQL(del, bindArgs);
 		}
 		catch (SQLException e)
 		{
 			Log.e("Transaction.erase", "erasing failed");
 			lootDB.endTransaction();
 			return false;
 		}
 		
 		if (!eraseTags())
 		{
 			Log.e("Transaction.erase", "tag erasing failed");
 			lootDB.endTransaction();
 			return false;
 		}
 		
 		// erase any repeat schedules, if available
 		int repeat = RepeatSchedule.getRepeatId(this.id);
 		if (repeat != -1)
 		{
 			RepeatSchedule rpt = RepeatSchedule.getSchedule(repeat);
 			if (!rpt.erase(false))
 			{
 				lootDB.endTransaction();
 				return false;
 			}
 		}
 				
 		lootDB.setTransactionSuccessful();
 		lootDB.endTransaction();
 		
 		return true;
 	}
 	
 	private static String[] privGetStrings(String sql)
 	{
 		SQLiteDatabase lootDB;
 		try
 		{
 			lootDB = Database.getDatabase();
 		}
 		catch ( SQLException e )
 		{
 			return null;
 		}
 		
 		Cursor cur = lootDB.rawQuery(sql, null);
 		
 		if (!cur.moveToFirst())
 		{
 			cur.close();
 			return null;
 		}
 
 		String[] result = new String[cur.getCount()];
 		int i = 0;
 		
 		do
 		{
 			result[i++] = cur.getString(0);
 		} while (cur.moveToNext());
 		cur.close();
 		
 		return result;
 	}
 	
 	public static String[] getAllStrings()
 	{
 		return privGetStrings("select name, count(*) as cnt from tags group by name union all " +
 				"select party, count(*) as cnt from transactions group by party order by cnt desc");
 	}
 	
 	public static String[] getAllTags()
 	{
 		return privGetStrings("select name, count(*) as cnt from tags " +
 				"group by name order by cnt desc");
 	}
 	
 	public static String[] getAllParties()
 	{
 		return privGetStrings("select party, count(*) as cnt from transactions " +
 				"group by party order by cnt desc");
 	}
 	
 	public void loadTags()
 	{
 		SQLiteDatabase lootDB = Database.getDatabase();
 		String[] columns = {"name"};
 		String[] sArgs = {Integer.toString(this.id)};
 		Cursor cur = lootDB.query("tags", columns, "trans_id = ?", sArgs, null, null, null);
 		
 		if (!cur.moveToFirst())
 		{
 			cur.close();
 			return;
 		}
 		
 		do
 		{
 			this.tags.add(cur.getString(0));
 		} while (cur.moveToNext());
 		cur.close();
 	}
 	
 	public void loadImages()
 	{
 		SQLiteDatabase lootDb = Database.getDatabase();
 		String[] columns = {"uri"};
 		String[] sArgs = {Integer.toString(this.id)};
 		Cursor cur = lootDb.query("images", columns, "trans_id = ?", sArgs, null, null, null);
 		
 		if (!cur.moveToFirst())
 		{
 			cur.close();
 			return;
 		}
 		
 		do
 		{
 			this.images.add(Uri.parse(cur.getString(0)));
 		} while (cur.moveToNext());
 		cur.close();
 	}
 	
 	public static Transaction getTransactionById(int id)
 	{
 		return getTransactionById(id, false);
 	}
 	
 	public static Transaction getTransactionById(int id, boolean purged)
 	{
 		String id_query = "id = ?";
 		if (!purged)
 			id_query += " and purged = 0";
 		
 		SQLiteDatabase lootDB = Database.getDatabase();
 		String[] columns = {"posted", "date", "party", "amount", "check_num", "account", "budget"};
 		String[] sArgs = {Integer.toString(id)};
 		Cursor cur = lootDB.query("transactions", columns, id_query, sArgs, null, null, null);
 		
 		if (!cur.moveToFirst())
 		{
 			cur.close();
 			return null;
 		}
 		
 		double amount = cur.getDouble(3);
 		int check = cur.getInt(4);
 		int type;
 		boolean posted = Database.getBoolean(cur.getInt(0));
 		boolean budget = Database.getBoolean(cur.getInt(6));
 		Date date = new Date(cur.getLong(1));
 		if ( check > 0 )
 		{
 			type = Transaction.CHECK;
 			amount = -amount;
 		}
 		else
 		{
 			if ( amount >= 0 )
 				type = Transaction.DEPOSIT;
 			else
 			{
 				type = Transaction.WITHDRAW;
 				amount = -amount;
 			}
 		}
 		
 		Transaction trans = new Transaction(posted, budget, date, type, cur.getString(2), amount, check);
 		trans.account = cur.getInt(5);
 		trans.id = id;
 		cur.close();
 		
 		trans.loadTags();
 		trans.loadImages();
 		
 		return trans;
 	}
 	
 	public void fromCursor(Cursor cur)
 	{
 		double amount = cur.getDouble(3);
 		int check = cur.getInt(4);
 		int type;
 		this.posted = Database.getBoolean(cur.getInt(0));
 		this.budget = Database.getBoolean(cur.getInt(6));
 		this.date = new Date(cur.getLong(1));
 		if ( check > 0 )
 		{
 			type = Transaction.CHECK;
 			amount = -amount;
 		}
 		else
 		{
 			if ( amount >= 0 )
 				type = Transaction.DEPOSIT;
 			else
 			{
 				type = Transaction.WITHDRAW;
 				amount = -amount;
 			}
 		}
 		
 		this.party = cur.getString(2);
 		this.check_num = check;
 		this.amount = amount;
 		this.type = type;
 		this.account = cur.getInt(5);
 		this.id = cur.getInt(7);
 	}
 	
 	public int transfer(Account acct2)
 	{
 		Account acct1 = Account.getAccountById(this.account);
 		if ( acct1 == null || acct2 == null )
 		{
 			return -1;
 		}
 	
 		String detail1, detail2;
 		
 		// check to see if we're updating a transfer or creating a new one
 		int trans_id2 = getTransferId();
 		Transaction trans2, old_trans = null;
 		trans2 = new Transaction(this, false);
 		boolean purged = false, update = false;
 		
 		if (trans_id2 > -1)
 		{
 			trans2.id = trans_id2;
 			update = true;
 			old_trans = Transaction.getTransactionById(trans_id2, false);
 			if (old_trans == null)
 			{
 				purged = true;
 				old_trans = Transaction.getTransactionById(trans_id2, true);
 			}
 		}
 
 		if ( this.type == Transaction.DEPOSIT )
 		{
 			if (acct1.credit || acct2.credit)
 			{
 				if (acct1.credit && acct2.credit)
 				{
 					trans2.type = Transaction.WITHDRAW;
 				}
 				else
 				{
 					trans2.type = Transaction.DEPOSIT;
 				}
 				detail1 = "to ";
 				detail2 = "from ";
 			}
 			else
 			{
 				trans2.type = Transaction.WITHDRAW;
 				detail1 = "from ";
 				detail2 = "to ";
 			}
 		}
 		else
 		{
 			if (this.type == Transaction.CHECK)
 			{
 				trans2.check_num = 0;
 			}
 			
 			detail1 = "to ";
 			detail2 = "from ";
 			
 			if (acct1.credit || acct2.credit)
 			{
 				if (acct1.credit && acct2.credit)
 				{
 					trans2.type = Transaction.DEPOSIT;
 				}
 				else
 				{
 					trans2.type = Transaction.WITHDRAW;
 				}
 				detail1 = "from ";
 				detail2 = "to ";
 			}
 			else
 			{
 				trans2.type = Transaction.DEPOSIT;
 				detail1 = "to ";
 				detail2 = "from ";
 			}
 		}
 
 		// sqlite craps out if we wrap all this in a transaction block
 		// so we have to assume that if there is an error, the erasures 
 		// always run successfully
 		
 		// write this transaction to the database
 		this.party = "Transfer " + detail1 + acct2.name;
 		int ret = this.write(acct1.id());
 		if (ret == -1)
 		{
 			return -1;
 		}
 		
 		trans2.party = "Transfer " + detail2 + acct1.name;
 		int transfer_id = trans2.write(acct2.id());
 
 		if (transfer_id == -1)
 		{
 			this.eraseTransaction();
 			return -1;
 		}
 		
 		if (!update)
 		{
 			// make sure both transfers succeeded and link them together
 			if (!linkTransfer(ret, transfer_id))
 			{
 				this.eraseTransaction();
 				trans2.eraseTransaction();
 				ret = -1;
 			}
 		}
 		else
 		{
 			if (purged)
 			{
 				// update the initialBalance of the secondary account if the transaction was purged
 				double a, b;
 				if (trans2.type == Transaction.WITHDRAW)
 				{
 					a = old_trans.amount;
 					b = trans2.amount;
 				}
 				else
 				{
 					a = trans2.amount;
 					b = old_trans.amount;
 				}
 				
 				acct2.initialBalance += a - b;
 				if (acct2.write() == -1)
 				{
 					this.eraseTransfer();
 					this.eraseTransaction();
 					trans2.eraseTransaction();
 					ret = -1;
 				}
 			}
 		}
 		
 		return ret;
 	}
 	
 	public boolean eraseTransfer()
 	{
 		Transaction trans2 = Transaction.getTransactionById(getTransferId(), false);
 		boolean purged = false;
 		if ( trans2 == null )
 		{
 			purged = true;
 			trans2 = Transaction.getTransactionById(getTransferId(), true);
 		}
 		if ( trans2 == null )
 		{
 			// remove the transfer link because the linked transaction no longer exists
 			trans2 = new Transaction();
 			removeTransfer(trans2);
 			return false;
 		}
 		if ( !this.eraseTransaction() || !trans2.eraseTransaction() )
 		{
 			return false;
 		}
 		
 		// if the second transaction has been purged, update the account balance accordingly
 		if (purged)
 		{
 			Account acct2 = Account.getAccountById(trans2.account);
 			double amt = trans2.amount;
 			if ( trans2.type == Transaction.WITHDRAW )
 			{
 				amt = -trans2.amount;
 			}
 			acct2.initialBalance -= amt;
 			acct2.write();
 		}
 		
 		// erase any repeat schedules, if available
 		int repeat = RepeatSchedule.getRepeatId(this.id);
 		if (repeat != -1)
 		{
 			RepeatSchedule rpt = RepeatSchedule.getSchedule(repeat);
 			if (!rpt.erase(true))
 			{
 				return false;
 			}
 		}
 		
 		return removeTransfer(trans2);
 	}
 	
 	public boolean linkTransfer(int id1, int id2)
 	{
 		if (!removeTransfer(Transaction.getTransactionById(id2)) || id1 == -1 || id2 == -1)
 		{
 			return false;
 		}
 				
 		String transfer = "insert into transfers (trans_id1, trans_id2) values (?,?)";
 		Object[] bindArgs = {new Long(id1), new Long(id2)};
 
 		SQLiteDatabase lootDB = Database.getDatabase();
 		lootDB.beginTransaction();
 
 		try
 		{
 			lootDB.execSQL(transfer, bindArgs);
 			lootDB.setTransactionSuccessful();
 		}
 		catch (SQLException e)
 		{
 			return false;
 		}
 		finally
 		{
 			lootDB.endTransaction();
 		}
 		
 		return true;
 	}
 	
 	public boolean removeTransfer(Transaction trans)
 	{
 		String del = "delete from transfers where trans_id1 in (?,?) or trans_id2 in (?,?)";
 		Long id1, id2;
 		id1 = new Long(this.id);
 		if (trans != null)
 			id2 = new Long(trans.id);
 		else
 			id2 = id1;
 		Object[] bindArgs = {id1, id2, id1, id2};
 		
 		SQLiteDatabase lootDB = Database.getDatabase();
 		lootDB.beginTransaction();
 		
 		try
 		{
 			lootDB.execSQL(del, bindArgs);
 			lootDB.setTransactionSuccessful();
 		}
 		catch (SQLException e)
 		{
 			return false;
 		}
 		finally
 		{
 			lootDB.endTransaction();
 		}
 		
 		return true;
 	}
 	
 	public int getTransferId()
 	{
 		SQLiteDatabase lootDB = Database.getDatabase();
 		String[] columns = {"trans_id1", "trans_id2"};
 		String selection = "trans_id1 = " + this.id + " or trans_id2 = " + this.id;
 		Cursor cur = lootDB.query("transfers", columns, selection, null, null, null, null);
 
 		int ret = -1;
 		if (!cur.moveToFirst())
 		{
 			cur.close();
 			return ret;
 		}
 		
 		ret = cur.getInt(0);
 		if (ret == this.id)
 			ret = cur.getInt(1);
 		
 		cur.close();
 		return ret;
 	}
 
 	public static void setComparator(int pcomp)
 	{
 		if (pcomp == COMP_DATE || pcomp == COMP_PARTY || pcomp == COMP_AMT)
 		{
 			comp = pcomp;
 			Database.setOption("sort_column", comp);
 		}
 	}
 
 	public static int getComparator()
 	{
 		// if comp hasn't been initialized, get the value from the database
 		if (comp < 0 || comp > 2)
 		{
 			comp = (int)Database.getOptionInt("sort_column");
 			if (comp < 0 || comp > 2)
 				comp = COMP_DATE;
 		}
 		return comp;
 	}
 
 	private int compareDates(Transaction t2)
 	{
 		return this.date.compareTo(t2.date);
 	}
 	
 	private int compareAmounts(Transaction t2)
 	{
 		// get the non-absolute value of the amount 
 		double a = (this.type == DEPOSIT ? this.amount : -this.amount),
 			b = (t2.type == DEPOSIT ? t2.amount : -t2.amount);
 		
 		int alb = 0, agb = 0;
 		if (a < b)
 			alb = 1;
 		if (a > b)
 			agb = 1;
 		return alb - agb;
 	}
 	
 	private int compareParties(Transaction t2)
 	{
 		return this.party.toLowerCase().compareTo(t2.party.toLowerCase());
 	}
 	
 	private int compareIds(Transaction t2)
 	{
 		int a = this.id,
 			b = t2.id;
 		
 		int alb = 0, agb = 0;
 		if (a > b)
 			agb = 1;
 		if (a < b)
 			alb = 1;
 		return agb - alb;
 	}
 
 	public int compareTo(Transaction t2)
 	{
 		// if comp hasn't been initialized, get the value from the database
 		if (comp < 0 || comp > 2)
 		{
 			comp = (int)Database.getOptionInt("sort_column");
 			if (comp < 0 || comp > 2)
 				comp = COMP_DATE;
 		}
 		
 		int ret = 0;
 		
 		if (comp == COMP_DATE)
 		{
 			ret = this.compareDates(t2);
 			if (ret == 0)
 			{
 				ret = this.compareAmounts(t2);
 				if (ret == 0)
 				{
 					ret = this.compareParties(t2);
 					if (ret == 0)
 						ret = this.compareIds(t2);
 				}
 			}
 		}
 		else if (comp == COMP_AMT)
 		{
 			ret = this.compareAmounts(t2);
 			if (ret == 0)
 			{
 				ret = this.compareParties(t2);
 				if (ret == 0)
 				{
 					ret = this.compareDates(t2);
 					if (ret == 0)
 						ret = this.compareIds(t2);
 				}
 			}
 		}
 		else if (comp == COMP_PARTY)
 		{
 			ret = this.compareParties(t2);
 			if (ret == 0)
 			{
 				ret = this.compareDates(t2);
 				if (ret == 0)
 				{
 					ret = this.compareAmounts(t2);
 					if (ret == 0)
 						ret = this.compareIds(t2);
 				}
 			}
 		}
 		
 		return ret;
 	}
 }
