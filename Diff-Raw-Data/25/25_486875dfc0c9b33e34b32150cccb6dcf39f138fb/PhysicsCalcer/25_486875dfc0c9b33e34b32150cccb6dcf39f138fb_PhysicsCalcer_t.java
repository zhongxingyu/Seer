 package de.engine.physics;
 
 import java.util.ArrayList;
 
 import de.engine.environment.EnvProps;
 import de.engine.math.Util;
 import de.engine.math.Vector;
 import de.engine.objects.Circle;
 import de.engine.objects.ObjectProperties;
 import de.engine.objects.Polygon;
 import de.engine.physics.ContactCreator.Contact;
 import de.engine.physics.colldetect.CollisionData;
 
 public class PhysicsCalcer
 {
     
     public static CollisionData run(CollisionData collPair)
     {
         if (collPair.obj1 instanceof Circle && collPair.obj2 instanceof Circle)
         {
             return PhysicsCalcer.calcCircles(collPair);
         }
         else if (collPair.obj1 instanceof Polygon && collPair.obj2 instanceof Polygon)
         {
             return PhysicsCalcer.calcPolygons(collPair);
         }
         else
         {
             return PhysicsCalcer.calcCirclePolygon(collPair);
         }
     }
     
     private static CollisionData calcCircles(CollisionData collPair)
     {
         Contact contact = ContactCreator.getCirclesContact((Circle) collPair.obj1, (Circle) collPair.obj2, collPair.coll_time);
         return resolveContact(collPair, contact);
     }
     
     private static CollisionData calcCirclePolygon(CollisionData collPair)
     {
         Contact contact = null;
         if (collPair.obj1 instanceof Circle && collPair.obj2 instanceof Polygon)
             contact = ContactCreator.getCirclePolygonContact((Circle) collPair.obj1, (Polygon) collPair.obj2, collPair.coll_time);
         else if (collPair.obj1 instanceof Polygon && collPair.obj2 instanceof Circle)
         {
             contact = ContactCreator.getCirclePolygonContact((Circle) collPair.obj2, (Polygon) collPair.obj1, collPair.coll_time);
             if (contact != null)
                 contact.normal.scale(-1.0);
         }
         return resolveContact(collPair, contact);
     }
     
     private static CollisionData calcPolygons(CollisionData collPair)
     {
         ArrayList<Contact> contacts = ContactCreator.getPolygonsContact((Polygon) collPair.obj1, (Polygon) collPair.obj2, collPair.coll_time);
         
         if (contacts.size() == 1)
         {
             return resolveContact(collPair, contacts.get(0));
         }
         else if (contacts.size() == 2)
         {
            Contact c = new Contact(Util.add(contacts.get(0).point, contacts.get(1).point).scale(0.5), contacts.get(0).normal, contacts.get(0).penetration);
            return resolveContact(collPair, c);
         }
         else if (contacts.size() == 4)
         {
             
             Contact c = new Contact(Util.add(contacts.get(0).point, contacts.get(1).point).scale(0.5), Util.add(contacts.get(0).normal, contacts.get(1).normal).getUnitVector(), contacts.get(0).penetration);
             return resolveContact(collPair, c);
         }
         return null;
     }
     
     /**
      * 
      * @param obj
      * @param contact
      * @param time
      * @return the relaive Postion between an Object and the Contact-Point
      */
     private static Vector getRelContactPos(ObjectProperties obj, Contact contact, double time)
     {
         return Util.minus(contact.point, obj.getPosition(time));
     }
     
     /**
      * @param relPos
      * @param velocity
      * @param angularV
      * @return the Velocity on th Contactpoint
      */
     private static Vector getContactVelocity(Vector relPos, Vector velocity, double angularV)
     {
        return new Vector(-1 * angularV * relPos.getY(), angularV * relPos.getX()).add(velocity);
     }
     
     private static double getJzaehler(Vector relVelocity, Vector normal, double elasticity)
     {
         return Util.scalarProduct(relVelocity, normal) * -(1 + elasticity);
     }
     
     /**
      * @param the Normal of the Contact
      * @param the relative Position of the Contact from the Object
      * @param the Mass of the Object
      * @param the Moment of Inertia of the Object
      * @return
      */
     private static double getJnennerPart(Vector normal, Vector rel_pos, double mass, double moment_of_inertia)
     {
         double r_o_cross_n = Util.crossProduct(rel_pos, normal);
         return (1 / mass) + (r_o_cross_n * r_o_cross_n) / moment_of_inertia;
     }
     
     private static Vector getJNormalforOne(ObjectProperties obj, double time, Contact contact, double elasticity)
     {
         Vector rel_pos = getRelContactPos(obj, contact, time);
         Vector pos_v = getContactVelocity(rel_pos, obj.velocity, obj.angular_velocity);
         double j_z = getJzaehler(pos_v, contact.normal, elasticity);
         double j_n = getJnennerPart(contact.normal, rel_pos, obj.getMass(), obj.moment_of_inertia);
         return Util.scale(contact.normal, j_z / j_n);
     }
     
     private static Vector getJNormalforTwo(ObjectProperties obj1, ObjectProperties obj2, double time, Contact contact)
     {
         Vector rel_pos1 = getRelContactPos(obj1, contact, time);
         Vector rel_pos2 = getRelContactPos(obj2, contact, time);
         Vector pos_v1 = getContactVelocity(rel_pos1, obj1.velocity, obj1.angular_velocity);
         Vector pos_v2 = getContactVelocity(rel_pos2, obj2.velocity, obj2.angular_velocity);
         double elasticity = (obj1.surface.elasticity() + obj2.surface.elasticity()) / 2;
         double j_z = getJzaehler(Util.minus(pos_v1, pos_v2), contact.normal, elasticity);
         double j_n = getJnennerPart(contact.normal, rel_pos1, obj1.getMass(), obj1.moment_of_inertia);
         j_n += getJnennerPart(contact.normal, rel_pos2, obj2.getMass(), obj2.moment_of_inertia);
         return Util.scale(contact.normal, j_z / j_n);
     }
     
     private static void updateVobj1(ObjectProperties obj, Vector rel_pos, Vector j_normal)
     {
         obj.velocity.add(Util.scale(j_normal, 1 / obj.getMass()));
         obj.angular_velocity += Util.crossProduct(rel_pos, j_normal) / obj.moment_of_inertia;
     }
     
     private static void updateVobj2(ObjectProperties obj, Vector rel_pos, Vector j_normal)
     {
         obj.velocity.minus(Util.scale(j_normal, 1 / obj.getMass()));
         obj.angular_velocity -= Util.crossProduct(rel_pos, j_normal) / obj.moment_of_inertia;
     }
     
     private static CollisionData getRestingContact(ObjectProperties obj1, ObjectProperties obj2, Vector coll_normal, double penetration)
     {
         double min_v = 1.0;
         
         Vector obj1_comp = Util.getVectorComponents(obj1.velocity, coll_normal, coll_normal.getNormalVector());
         Vector obj2_comp = Util.getVectorComponents(obj2.velocity, coll_normal, coll_normal.getNormalVector());
         Vector normal_part1 = Util.scale(coll_normal, obj1_comp.getX());
         Vector normal_part2 = Util.scale(coll_normal, obj2_comp.getX());
         
         //abhänigkeit zu fallbeschleunigung einbauen
         if (((-1 * min_v < normal_part1.getX() && normal_part1.getX() < min_v) && (-1 * min_v < normal_part1.getY() && normal_part1.getY() < min_v)) && ((-1 * min_v < normal_part2.getX() && normal_part2.getX() < min_v) && (-1 * min_v < normal_part2.getY() && normal_part2.getY() < min_v)))
         {
             CollisionData restingContact = new CollisionData(obj1, obj2, 0.0, EnvProps.deltaTime());
             restingContact.coll_time = EnvProps.deltaTime();
             restingContact.calc_time = EnvProps.deltaTime();
             if(!obj1.isPinned)
                 obj1.world_position.translation.add(Util.scale(coll_normal, penetration));
             if(!obj2.isPinned)
                 obj2.world_position.translation.add(Util.scale(coll_normal, -1 * penetration));
             //obj1.velocity.minus(normal_part1);
             //obj2.velocity.minus(normal_part2);
             return restingContact;
         }
         
         return null;
     }
     
     private static CollisionData resolveContact(CollisionData collPair, Contact contact)
     {
         if (contact == null)
             return null;
         if (collPair.obj1.isPinned && collPair.obj2.isPinned)
             return null;
         
         Vector j_normal;
         double elasticity = (collPair.obj1.surface.elasticity() + collPair.obj2.surface.elasticity()) / 2;
         
         if (collPair.obj2.isPinned)
         {
             if (!contact.from_second)
                 contact.normal.scale(-1);
             j_normal = getJNormalforOne(collPair.obj1, collPair.coll_time, contact, elasticity);
             Vector rel_pos = getRelContactPos(collPair.obj1, contact, collPair.coll_time);
             
             collPair.obj1.update(collPair.coll_time);
             
             updateVobj1(collPair.obj1, rel_pos, j_normal);
             
             return getRestingContact(collPair.obj1, collPair.obj2, contact.normal, contact.penetration);
         }
         else if (collPair.obj1.isPinned)
         {
             if (contact.from_second)
                 contact.normal.scale(-1);
             j_normal = getJNormalforOne(collPair.obj2, collPair.coll_time, contact, elasticity);
             Vector rel_pos = getRelContactPos(collPair.obj2, contact, collPair.coll_time);
             
             collPair.obj2.update(collPair.coll_time);
             
             updateVobj1(collPair.obj2, rel_pos, j_normal);
             
             return getRestingContact(collPair.obj2, collPair.obj1, contact.normal, contact.penetration);
         }
         else
         {
             if (!contact.from_second)
                 contact.normal.scale(-1);
             j_normal = getJNormalforTwo(collPair.obj1, collPair.obj2, collPair.coll_time, contact);
             
             collPair.obj1.update(collPair.coll_time);
             collPair.obj2.update(collPair.coll_time);
             
             Vector rel_pos1 = getRelContactPos(collPair.obj1, contact, collPair.coll_time);
             Vector rel_pos2 = getRelContactPos(collPair.obj2, contact, collPair.coll_time);
             
             updateVobj1(collPair.obj1, rel_pos1, j_normal);
             updateVobj2(collPair.obj2, rel_pos2, j_normal);
             
             return getRestingContact(collPair.obj1, collPair.obj2, contact.normal, contact.penetration);
         }
     }
     
     // Quelle: http://www.myphysicslab.com/collision.html
     private static CollisionData resolveContact2(CollisionData collPair, Contact contact)
     {
         if (contact == null)
             return null;
         
         Vector coll_point = contact.point;
         Vector coll_normal = contact.normal;
         
         Vector r_o1 = Util.minus(coll_point, collPair.obj1.getPosition(collPair.coll_time));
         Vector r_o2 = Util.minus(coll_point, collPair.obj2.getPosition(collPair.coll_time));
         Vector v_o1 = new Vector();
         if (!collPair.obj1.isPinned)
             v_o1 = new Vector(collPair.obj1.angular_velocity * r_o1.getX(), collPair.obj1.angular_velocity * r_o1.getY()).add(collPair.obj1.velocity);
         Vector v_o2 = new Vector();
         if (!collPair.obj2.isPinned)
             v_o2 = new Vector(collPair.obj2.angular_velocity * r_o2.getX(), collPair.obj2.angular_velocity * r_o2.getY()).add(collPair.obj2.velocity);
         
         double r_o1_cross_n = Util.crossProduct(r_o1, coll_normal);
         double r_o2_cross_n = Util.crossProduct(r_o2, coll_normal);
         
         Vector v_rel = Util.minus(v_o1, v_o2);
         double j_z = Util.scalarProduct(v_rel, coll_normal) * -(1 + (collPair.obj1.surface.elasticity() + collPair.obj2.surface.elasticity()) / 2);
         double j_n = 0;
         
         if (!collPair.obj2.isPinned)
             j_n += (1 / collPair.obj2.getMass()) + (r_o2_cross_n * r_o2_cross_n) / collPair.obj2.moment_of_inertia;
         if (!collPair.obj1.isPinned)
             j_n += (1 / collPair.obj1.getMass()) + (r_o1_cross_n * r_o1_cross_n) / collPair.obj1.moment_of_inertia;
         
         double j = j_z / j_n;
         
         // weird hack, but solves a problem in square_n_circles.scnx, will check this later again
         // if(j < 0 && !collPair.obj1.isPinned && !collPair.obj2.isPinned) j *= -1.0;
         // if (j < 0 && (collPair.obj1 instanceof Polygon || collPair.obj2 instanceof Polygon)) j *= -1.0;
         
         Vector j_normal = Util.scale(coll_normal, j);
         
         double min_v = 1.0;
         
         if (!collPair.obj1.isPinned)
         {
             if (collPair.obj1.sleeps())
                 collPair.obj1.wakeUp();
             collPair.obj1.update(collPair.coll_time);
             
             collPair.obj1.velocity.add(Util.scale(j_normal, 1 / collPair.obj1.getMass()));
             collPair.obj1.angular_velocity += Util.crossProduct(r_o1, j_normal) / collPair.obj1.moment_of_inertia;
             
             if ((collPair.obj2.isPinned || collPair.obj2.sleeps()) && ((-1 * min_v < collPair.obj1.velocity.getX() && collPair.obj1.velocity.getX() < min_v) && (-1 * min_v < collPair.obj1.velocity.getY() && collPair.obj1.velocity.getY() < min_v)))
             {
                 collPair.obj1.velocity = new Vector();
                 collPair.obj1.fallAsleep(collPair.obj2);
             }
             
         }
         
         if (!collPair.obj2.isPinned)
         {
             if (collPair.obj2.sleeps())
                 collPair.obj2.wakeUp();
             collPair.obj2.update(collPair.coll_time);
             
             collPair.obj2.velocity.minus(Util.scale(j_normal, 1 / collPair.obj2.getMass()));
             collPair.obj2.angular_velocity -= Util.crossProduct(r_o2, j_normal) / collPair.obj2.moment_of_inertia;
             
             if ((collPair.obj1.isPinned || collPair.obj1.sleeps()) && ((-1 * min_v < collPair.obj2.velocity.getX() && collPair.obj2.velocity.getX() < min_v) && (-1 * min_v < collPair.obj2.velocity.getY() && collPair.obj2.velocity.getY() < min_v)))
             {
                 collPair.obj2.velocity = new Vector();
                 collPair.obj2.fallAsleep(collPair.obj1);
             }
             
         }
         
         return getRestingContact(collPair.obj1, collPair.obj2, coll_normal, contact.penetration);
     }
 }
