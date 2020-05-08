 /*
  * gw2live - GuildWars 2 Dynamic Map
  * 
  * Website: http://gw2map.com
  *
  * Copyright 2013   zyclonite    networx
  *                  http://zyclonite.net
  * Developer: Lukas Prettenthaler
  */
 package net.zyclonite.gw2live.listener;
 
 import com.espertech.esper.client.EventBean;
 import java.util.Date;
 import net.zyclonite.gw2live.model.WvwGuildStatistic;
 import net.zyclonite.gw2live.service.MongoDB;
 import net.zyclonite.gw2live.util.EplUpdateListener;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 /**
  *
  * @author zyclonite
  */
 public class GuildStatisticUpdateListener extends EplUpdateListener {
 
     private static final Log LOG = LogFactory.getLog(GuildStatisticUpdateListener.class);
    private static final String EPL = "select o.* from pattern [ every o=WvwEvent -> every n=WvwEvent(match_id=o.match_id, map_type=o.map_type, objective_id=o.objective_id) ] where n.owner_guild != o.owner_guild and o.owner_guild is not null";
     private final MongoDB db;
 
     public GuildStatisticUpdateListener() {
         super("GuildStatisticUpdateListener", EPL);
         db = MongoDB.getInstance();
         LOG.debug("GuildStatisticUpdateListener initialized");
     }
 
     @Override
     public void update(EventBean[] newEvents, EventBean[] oldEvents) {
         //TODO: needs threading (nonblocking) + push to eventbus
        final Date now = new Date();
        for (final EventBean event : newEvents) {
            final Date oldtime = (Date) event.get("guild_timestamp");
             final WvwGuildStatistic gstats = new WvwGuildStatistic();
            gstats.setGuild_id(event.get("owner_guild").toString());
             gstats.setHoldtime(now.getTime() - oldtime.getTime());
            gstats.setMap_type(event.get("map_type").toString());
            gstats.setMatch_id(event.get("match_id").toString());
            gstats.setObjective_id(Long.parseLong(event.get("objective_id").toString()));
             gstats.setTimestamp(now);
             db.saveWvwGuildStatistics(gstats);
         }
     }
 }
