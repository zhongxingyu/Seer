 package ru.redsolution.bst.data.serializer;
 
 import java.io.IOException;
 import java.io.StringWriter;
 
 import org.xmlpull.v1.XmlSerializer;
 
 import ru.redsolution.bst.data.tables.SelectedTable;
 import android.database.Cursor;
 import android.util.Xml;
 
 /**
  * Базовый класс для сериализации данных для отправки.
  * 
  * @author alexander.ivanov
  * 
  */
 public abstract class BaseSerializer {
 
 	/**
 	 * @return XML для отправки.
 	 */
 	public String getXml() throws IllegalArgumentException,
 			IllegalStateException, IOException {
 		XmlSerializer serializer = Xml.newSerializer();
 		StringWriter writer = new StringWriter();
 		serializer.setOutput(writer);
 		serializer.startDocument("UTF-8", true);
 		serializer.startTag("", getContainerName());
 		renderContainerAttrs(serializer);
 		renderContainerBody(serializer);
 		serializer.endTag("", getContainerName());
 		serializer.endDocument();
		return serializer.toString();
 	}
 
 	/**
 	 * @return Имя контейнера.
 	 */
 	protected abstract String getContainerName();
 
 	/**
 	 * Задаёт атрибуты для контейнера.
 	 * 
 	 * @param serializer
 	 * @throws IllegalArgumentException
 	 * @throws IllegalStateException
 	 * @throws IOException
 	 */
 	protected abstract void renderContainerAttrs(XmlSerializer serializer)
 			throws IllegalArgumentException, IllegalStateException, IOException;
 
 	/**
 	 * Задаёт контент для контейнера.
 	 * 
 	 * @param serializer
 	 * @throws IllegalArgumentException
 	 * @throws IllegalStateException
 	 * @throws IOException
 	 */
 	protected void renderContainerBody(XmlSerializer serializer)
 			throws IllegalArgumentException, IllegalStateException, IOException {
 		Cursor cursor = getCursor();
 		try {
 			if (cursor.moveToFirst()) {
 				do {
 					serializer.startTag("", getItemName());
 					renderItemAttrs(serializer, cursor);
 					renderItemBody(serializer, cursor);
 					serializer.endTag("", getItemName());
 				} while (cursor.moveToNext());
 			}
 		} finally {
 			cursor.close();
 		}
 	}
 
 	/**
 	 * @return Имя элемента.
 	 */
 	protected abstract String getItemName();
 
 	/**
 	 * Задаёт атрибуты для элемента.
 	 * 
 	 * @param serializer
 	 * @param cursor
 	 * @throws IllegalArgumentException
 	 * @throws IllegalStateException
 	 * @throws IOException
 	 */
 	protected abstract void renderItemAttrs(XmlSerializer serializer,
 			Cursor cursor) throws IllegalArgumentException,
 			IllegalStateException, IOException;
 
 	/**
 	 * Задаёт контент для элемента.
 	 * 
 	 * @param serializer
 	 * @param cursor
 	 * @throws IllegalArgumentException
 	 * @throws IllegalStateException
 	 * @throws IOException
 	 */
 	protected void renderItemBody(XmlSerializer serializer, Cursor cursor)
 			throws IllegalArgumentException, IllegalStateException, IOException {
 
 	}
 
 	/**
 	 * @return Курсор с объектами.
 	 */
 	protected Cursor getCursor() {
 		return SelectedTable.getInstance().list();
 	}
 
 }
