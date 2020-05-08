 /* Copyright (c) 2009 Henrik Gustafsson <henrik.gustafsson@fnord.se>
  *
  * Permission to use, copy, modify, and distribute this software for any
  * purpose with or without fee is hereby granted, provided that the above
  * copyright notice and this permission notice appear in all copies.
  *
  * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
  * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
  * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
  * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
  * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
  * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
  * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
  */
 
 package se.fnord.xmms2.client.commands;
 
 import se.fnord.xmms2.client.commands.internal.BasicCommand;
 import se.fnord.xmms2.client.commands.internal.CollectionChangedBroadcastCommand;
 import se.fnord.xmms2.client.commands.internal.CollectionListCommand;
 import se.fnord.xmms2.client.internal.IpcCommand;
 import se.fnord.xmms2.client.internal.IpcObject;
 import se.fnord.xmms2.client.types.CollectionExpression;
 import se.fnord.xmms2.client.types.CollectionNamespace;
 import se.fnord.xmms2.client.types.IdQuery;
 import se.fnord.xmms2.client.types.Query;
 
 public final class Collection {
 
 	public static Command changeBroadcast() {
 		return new CollectionChangedBroadcastCommand();
 	}
 
 	public static Command get(CollectionNamespace namespace, String name) {
 		return new BasicCommand(IpcObject.COLLECTION, IpcCommand.COLLECTION_GET, name, namespace);
 	}
 
 	public static Command list(CollectionNamespace namespace) {
 		return new CollectionListCommand(namespace);
 	}
 
 	public static Command query(Query q) {
 		if (q instanceof IdQuery)
 			return new BasicCommand(IpcObject.COLLECTION, IpcCommand.QUERY_IDS, q);
 
 		return new BasicCommand(IpcObject.COLLECTION, IpcCommand.QUERY_INFOS, q);
 	}
 
 	public static Command save(CollectionNamespace namespace, String name, CollectionExpression expression) {
		return new BasicCommand(IpcObject.COLLECTION, IpcCommand.COLLECTION_SAVE, name, namespace.toString(), expression);
	}
	
	public static Command remove(CollectionNamespace namespace, String name) {
		return new BasicCommand(IpcObject.COLLECTION, IpcCommand.COLLECTION_REMOVE, name, namespace.toString());
 	}
 
 }
