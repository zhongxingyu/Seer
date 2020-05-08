 package lab2;
 
 public class ListeChaine
 {
 	private Noeud tete;
 	private Noeud explorer;
 	private int size = 0;
 
 	public ListeChaine()
 	{
 		tete = new Noeud(null, null);
 		explorer = tete;
 	}
 
 	public void addItem(AbstractCustomShape shape)
 	{
 		if (tete.getObject() == null)
 		{
 			tete.setObject(shape);
 		} 
 		else
 		{
 			Noeud looker = tete;
 			Noeud newKnot = null;
 			while (looker.hasNext())
 			{
 				looker = looker.getNext();
 			}
 			newKnot = new Noeud(shape, looker);
 			looker.setForward(newKnot);
 		}
 		size++;
 	}
 	
 	public void removeItem(int num)
 	{
 		if(num <= size)
 		{
 			Noeud looker = tete;
			boolean finish = true;
 			int i = 0;
 			
			while(looker.hasNext() && finish)
 			{
 				if(i >= num)
 				{
					if(looker.getNext() != null && i != 0)
 					{
 						looker.getNext().setBackward(looker.getLast());
 						looker.getLast().setForward(looker.getNext());
 					}
					else if(looker.getNext() != null && i == 0)
					{
						tete = looker.getNext();
						tete.setBackward(null);
					}
 					else
 					{
 						looker.getLast().setForward(null);
 					}
 					looker = null;
					finish = false;
 				}
 				++i;
 			}
 			size--;
 		}
 		else
 		{
 			System.out.println("Erreure ListeChaine, remove num > size");
 		}
 	}
 	
 	public int getSize()
 	{
 		System.out.println(String.valueOf(size));
 		return size;
 	}
 
 	public AbstractCustomShape getItem(int num)
 	{
 		int i = 0;
 		Noeud looker = tete;
 		boolean finish = false;
 		while (looker.hasNext() || finish)
 		{
 			i++;
 			looker = looker.getNext();
 			if (i >= num)
 			{
 				return (AbstractCustomShape) looker.getObject();
 			}
 		}
 		return null;
 	}
 
 	public AbstractCustomShape getNext()
 	{
 		explorer = explorer.getNext();
 		return (AbstractCustomShape) explorer.getLast().getObject();
 	}
 }
